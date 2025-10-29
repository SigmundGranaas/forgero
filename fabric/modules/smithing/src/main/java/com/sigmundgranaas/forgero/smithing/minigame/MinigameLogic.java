package com.sigmundgranaas.forgero.smithing.minigame;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.condition.PredicateConditionLootRegistry;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import lombok.Getter;
import lombok.Setter;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

@Getter
public class MinigameLogic {
    public static final int INITIAL_MARKER_DELAY_TICKS = 25;
    public static final int SUBSEQUENT_MARKER_DELAY_TICKS = 15;
    public static final int MARKER_LIFETIME_TICKS_NORMAL = 35;
    public static final int MARKER_LIFETIME_TICKS_FAST = 20;
    public static final int TOTAL_MARKERS = 10;
    public static final int FAST_MARKERS = 3; // Changed from 5 to 3

    private static final double MARKER_HIT_RADIUS_SQ = 0.0075d;

    private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
    private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
    private static final String FAST_MARKER_HITS_NBT_KEY = "forgero_fastMarkerHits";
    private static final String MISS_MARKER_NBT_KEY = "forgero_missMarkerHits";

    private final List<Vec2f> markerPositions = new ArrayList<>();
    private final List<Boolean> markerHits = new ArrayList<>();
    private final List<Integer> hitTemperatures = new ArrayList<>();
    // New: stage index per successful hit (0..5)
    private final List<Integer> hitStageIndices = new ArrayList<>();
    private final List<Integer> fastMarkerIndices = new ArrayList<>();
    private final Random random = new Random();

    private int coldStageHits = 0;
    private int warmStageHits = 0;
    private int hotStageHits = 0;
    private int veryHotStageHits = 0;
    private int nearMeltStageHits = 0;
    private int moltenStageHits = 0;
    private int fastMarkerHits = 0;
    private int missMarkerHits = 0;

    @Setter
    private int markerAttempts = 0;
    @Setter
    private int markerHitsCount = 0;

    private int markerTimeout = 0;
    private int markerSpawnDelay;
    private double morphProgress = 0.0;

    // Cached images for morphing/minigame
    private transient BufferedImage startingItemImage = null;
    private transient BufferedImage plannedProductImage = null;

    public interface MinigameCallback {
        void markDirty();
        void playHitEffect(Vec2f markerLocalPos);
        void playMissEffect();
        void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack);
        World getWorld();
        BlockPos getPos();
        BlockState getCachedState();
        ItemStack getCurrentStack();
        void replaceWithResult(ItemStack resultStack);
    }

    public MinigameLogic() {
        this.markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
    }

    public void resetMarkerProgress(MinigameCallback callback) {
        markerPositions.clear();
        markerHits.clear();
        markerAttempts = 0;
        markerHitsCount = 0;
        missMarkerHits = 0;
        markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
        fastMarkerIndices.clear();

        // Ensure first marker (index 0) is never a fast marker
        while (fastMarkerIndices.size() < FAST_MARKERS) {
            int idx = 1 + random.nextInt(TOTAL_MARKERS - 1); // Only indices 1..TOTAL_MARKERS-1
            if (!fastMarkerIndices.contains(idx)) {
                fastMarkerIndices.add(idx);
            }
        }

        ItemStack stack = callback.getCurrentStack();
        if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (nbt.contains(HITS_NBT_KEY) || nbt.contains(ATTEMPTS_NBT_KEY)) {
                this.markerHitsCount = nbt.getInt(HITS_NBT_KEY);
                this.markerAttempts = nbt.getInt(ATTEMPTS_NBT_KEY);
                this.morphProgress = nbt.getDouble("morphProgress");
                this.fastMarkerHits = nbt.getInt(FAST_MARKER_HITS_NBT_KEY);
                this.missMarkerHits = nbt.contains(MISS_MARKER_NBT_KEY) ? nbt.getInt(MISS_MARKER_NBT_KEY) : 0;
            } else {
                this.morphProgress = 0.0;
                this.fastMarkerHits = 0;
                this.missMarkerHits = 0;
            }
            // Removed restoration of continuous stage ticks from item NBT.
        } else {
            this.morphProgress = 0.0;
            this.fastMarkerHits = 0;
            this.missMarkerHits = 0;
            // Removed resets for continuous stage ticks (they no longer exist).
        }

        updateMorphProgressOnItem(stack);
        refreshMorphImages(callback);
        callback.markDirty();
    }

    public void clearMarkerProgress() {
        markerPositions.clear();
        markerHits.clear();
        markerAttempts = 0;
        markerHitsCount = 0;
        markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
        fastMarkerIndices.clear();
        fastMarkerHits = 0;
        missMarkerHits = 0;
        // Do not clear per-hit stage indices and temperatures for a fresh run
        hitStageIndices.clear();
        hitTemperatures.clear();
    }

    public boolean processHit(Vec2f itemLocalHit, MinigameCallback callback) {
        if (markerPositions.size() == 1) {
            Vec2f marker = markerPositions.get(0);
            if (marker.distanceSquared(itemLocalHit) < MARKER_HIT_RADIUS_SQ) {
                setMarkerHit(0, callback);
                return true;
            }
        }
        return false;
    }

    public void processMarkerAttempt(boolean hit, MinigameCallback callback) {
        processMarkerAttempt(hit, true, callback);
    }

    public void processMarkerAttempt(boolean hit, boolean wasPlayerAttempt, MinigameCallback callback) {
        if (markerHitsCount >= TOTAL_MARKERS) return;

        // Count attempts only when initiated by player
        if (wasPlayerAttempt) {
            markerAttempts++;
        }

        ItemStack stack = callback.getCurrentStack();

        if (hit) {
            // Successful hit
            markerHitsCount++;

            // Record stage index at time of hit
            int temperature = TemperatureUtils.getTemperature(stack);
            int maxTemp = TemperatureUtils.getMaxTemp(stack);
            hitStageIndices.add(stageIndexFor(temperature, maxTemp));

            // Apply temperature change based on marker type
            int markerIndex = markerAttempts - 1;
            boolean fast = fastMarkerIndices.contains(markerIndex);
            int tempChange = fast ? -10 : 40;
            TemperatureUtils.setTemperature(stack, Math.max(0, Math.min(temperature + tempChange, maxTemp)));

            if (fast) {
                fastMarkerHits++;
            }
        } else {
            // Miss: only count visible player attempts
            if (wasPlayerAttempt) {
                missMarkerHits++;
            }
        }

        // Clear current marker so next can spawn after a short delay
        clearActiveMarker();
        markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;

        // Update morph progress on the item so client HUD has up-to-date data
        updateMorphProgressOnItem(stack);

        // Persist counters to item NBT (optional but helps with consistency if picked up immediately)
        saveProgressToItem(stack);

        // Trigger BE sync to clients so HUD updates right away
        callback.markDirty();
    }

    // Map temp to stage index [0..5]
    private int stageIndexFor(int temperature, int maxTemp) {
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInCold(temperature, maxTemp)) return 0;
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInWarm(temperature, maxTemp)) return 1;
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInHot(temperature, maxTemp)) return 2;
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInBrightHot(temperature, maxTemp)) return 3;
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInOverheated(temperature, maxTemp)) return 4;
        return 5; // molten fallback
    }

    private void updateTemperatureStageHits(int temperature, int maxTemp) {
        if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInCold(temperature, maxTemp)) {
            coldStageHits++;
        } else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInWarm(temperature, maxTemp)) {
            warmStageHits++;
        } else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInHot(temperature, maxTemp)) {
            hotStageHits++;
        } else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInHot(temperature, maxTemp)) {
            veryHotStageHits++;
        } else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInBrightHot(temperature, maxTemp)) {
            nearMeltStageHits++;
        } else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInOverheated(temperature, maxTemp)) {
            moltenStageHits++;
        }
    }

    public void setMarkerHit(int index, MinigameCallback callback) {
        World world = callback.getWorld();
        if (world != null && !world.isClient && index >= 0 && index < markerHits.size()) {
            markerHits.set(index, true);
            callback.playHitEffect(markerPositions.get(index));
            clearActiveMarker();
            callback.markDirty();
        }
    }

    public void tick(MinigameCallback callback) {
        ItemStack stackForMarker = callback.getCurrentStack();
        boolean activeMorph = isMorphingActive(stackForMarker);

        if (!activeMorph || stackForMarker.isEmpty()) {
            if (!markerPositions.isEmpty() || markerSpawnDelay > 0) {
                clearMarkerProgress();
                callback.markDirty();
            }
            return;
        }

        // Temperature gating for marker spawning
        int temperature = TemperatureUtils.getTemperature(stackForMarker);
        int maxTemp = TemperatureUtils.getMaxTemp(stackForMarker);
        boolean hotEnoughForWork = com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp);
        if (!hotEnoughForWork) {
            return;
        }

        // Marker lifecycle
        if (markerPositions.isEmpty()) {
            if (markerSpawnDelay > 0) {
                markerSpawnDelay--;
            }
            if (markerSpawnDelay == 0) {
                Vec2f marker = nextMarkerPosition(stackForMarker, callback);
                if (marker.equals(Vec2f.ZERO)) {
                    markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
                    return;
                }
                markerPositions.add(marker);
                markerHits.add(false);
                markerTimeout = fastMarkerIndices.contains(markerAttempts)
                        ? MARKER_LIFETIME_TICKS_FAST
                        : MARKER_LIFETIME_TICKS_NORMAL;
                callback.markDirty();
                callback.spawnMarkerAppearanceEffect(marker, stackForMarker);
            }
        } else {
            markerTimeout--;
            if (markerTimeout <= 0) {
                if (fastMarkerIndices.contains(markerAttempts)) {
                    fastMarkerIndices.remove(Integer.valueOf(markerAttempts));
                    callback.markDirty();
                }
                processMarkerAttempt(false, false, callback);
            }
        }
    }

    public boolean isComplete() {
        return markerHitsCount >= TOTAL_MARKERS;
    }

    public void setMorphProgress(double progress, MinigameCallback callback, @SuppressWarnings("unused") Identifier plannedProductId) {
        this.morphProgress = progress;
        if (progress >= 1.0) {
            ItemStack stack = callback.getCurrentStack();
            if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
                Item resultItem = MorphedItem.getResultItem(stack);
                if (resultItem != null) {
                    ItemStack resultStack = new ItemStack(resultItem, stack.getCount());

                    // Apply conditions to result item using predicates
                    var stateOpt = StateService.INSTANCE.convert(resultStack);
                    if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional) {
                        var state = stateOpt.get();

                        MatchContext context = createMatchContext(callback, stack);

                        // Check for direct condition assignment using predicates
                        com.sigmundgranaas.forgero.core.condition.NamedCondition directCondition = PredicateConditionLootRegistry.getCondition(context);
                        if (directCondition != null) {
                            var conditioned = conditional.applyCondition(directCondition);
                            var newStackOpt = StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State) conditioned);
                            if (newStackOpt.isPresent()) {
                                resultStack = newStackOpt.get();
                            }
                        } else {
                            var lootTable = PredicateConditionLootRegistry.getLootTable(context);
                            if (lootTable.isEmpty()) {
                                lootTable = PredicateConditionLootRegistry.NEUTRAL;
                            }
                            // Filter lootTable to only include conditions whose target matches the state
                            List<com.sigmundgranaas.forgero.core.condition.NamedCondition> applicableConditions = lootTable.stream()
                                    .filter(cond -> cond.matches(state))
                                    .toList();
                            if (!applicableConditions.isEmpty()) {
                                com.sigmundgranaas.forgero.core.condition.NamedCondition randomCondition = applicableConditions.get(new Random().nextInt(applicableConditions.size()));
                                var conditioned = conditional.applyCondition(randomCondition);
                                var newStackOpt = StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State) conditioned);
                                if (newStackOpt.isPresent()) {
                                    resultStack = newStackOpt.get();
                                }
                            }
                        }
                    }

                    // Copy temperature from morphed item to result item
                    double currentTemp = TemperatureUtils.getTemperature(stack);
                    TemperatureUtils.setTemperature(resultStack, (int) Math.round(currentTemp));
                    callback.replaceWithResult(resultStack);

                    World world = callback.getWorld();
                    if (world != null && !world.isClient) {
                        world.playSound(null, callback.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }
            }
        }
        callback.markDirty();
    }

    private MatchContext createMatchContext(MinigameCallback callback, ItemStack stack) {
        MatchContext context = MatchContext.of();
        World world = callback.getWorld();
        if (world != null) {
            context = context.put(MinecraftContextKeys.WORLD, world);
            context = context.put(MinecraftContextKeys.BLOCK_TARGET, callback.getPos());
        }
        context = context.put(MinecraftContextKeys.STACK, stack);
        context = context.put(MinecraftContextKeys.COLD_STAGE_HITS, coldStageHits);
        context = context.put(MinecraftContextKeys.WARM_STAGE_HITS, warmStageHits);
        context = context.put(MinecraftContextKeys.HOT_STAGE_HITS, hotStageHits);
        context = context.put(MinecraftContextKeys.VERY_HOT_STAGE_HITS, veryHotStageHits);
        context = context.put(MinecraftContextKeys.NEAR_MELT_STAGE_HITS, nearMeltStageHits);
        context = context.put(MinecraftContextKeys.MOLTEN_STAGE_HITS, moltenStageHits);
        context = context.put(MinecraftContextKeys.TOTAL_HITS, markerHitsCount);
        context = context.put(MinecraftContextKeys.MISS_HITS, missMarkerHits);
        context = context.put(MinecraftContextKeys.FAST_MARKER_HITS, fastMarkerHits);

        // Removed continuous stage tick data and fraction exposure to match context.

        return context;
    }

    public void saveProgressToItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            NbtCompound itemNbt = stack.getOrCreateNbt();
            itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
            itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
            itemNbt.putInt(MISS_MARKER_NBT_KEY, missMarkerHits);
            itemNbt.putIntArray("fastMarkerIndices", fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray());
            itemNbt.putInt(FAST_MARKER_HITS_NBT_KEY, fastMarkerHits);
            // Persist per-hit stage indices for HUD coloring on resume
            itemNbt.putIntArray("hitStageIndices", hitStageIndices.stream().mapToInt(Integer::intValue).toArray());

            // Removed saving of continuous stage ticks.
        }
    }

    public void writeNbt(NbtCompound nbt) {
        // Store marker positions
        NbtCompound markersNbt = new NbtCompound();
        for (int i = 0; i < markerPositions.size(); i++) {
            Vec2f pos = markerPositions.get(i);
            NbtCompound markerNbt = new NbtCompound();
            markerNbt.putFloat("x", pos.x);
            markerNbt.putFloat("y", pos.y);
            markerNbt.putBoolean("hit", markerHits.size() > i && markerHits.get(i));
            markersNbt.put("marker_" + i, markerNbt);
        }
        nbt.put("markers", markersNbt);

        // Store temperature tracking data
        nbt.putIntArray("hitTemperatures", hitTemperatures.stream().mapToInt(Integer::intValue).toArray());
        // Persist per-hit stage indices
        nbt.putIntArray("hitStageIndices", hitStageIndices.stream().mapToInt(Integer::intValue).toArray());
        nbt.putInt("coldStageHits", coldStageHits);
        nbt.putInt("warmStageHits", warmStageHits);
        nbt.putInt("hotStageHits", hotStageHits);
        nbt.putInt("veryHotStageHits", veryHotStageHits);
        nbt.putInt("nearMeltStageHits", nearMeltStageHits);
        nbt.putInt("moltenStageHits", moltenStageHits);
        nbt.putInt("missHits", missMarkerHits);
        nbt.putIntArray("fastMarkerIndices", fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray());
        nbt.putDouble("morphProgress", morphProgress);

        // Removed storage of continuous stage ticks.
    }

    public void readNbt(NbtCompound nbt) {
        markerPositions.clear();
        markerHits.clear();

        if (nbt.contains("markers", NbtCompound.COMPOUND_TYPE)) {
            NbtCompound markersNbt = nbt.getCompound("markers");
            for (int i = 0; i < TOTAL_MARKERS; i++) {
                if (markersNbt.contains("marker_" + i)) {
                    NbtCompound markerNbt = markersNbt.getCompound("marker_" + i);
                    markerPositions.add(new Vec2f(markerNbt.getFloat("x"), markerNbt.getFloat("y")));
                    markerHits.add(markerNbt.getBoolean("hit"));
                }
            }
        }

        // Restore temperature tracking data
        hitTemperatures.clear();
        if (nbt.contains("hitTemperatures")) {
            int[] temps = nbt.getIntArray("hitTemperatures");
            for (int temp : temps) {
                hitTemperatures.add(temp);
            }
        }
        // Restore per-hit stage indices (optional, fallback computed HUD-side if missing)
        hitStageIndices.clear();
        if (nbt.contains("hitStageIndices")) {
            int[] arr = nbt.getIntArray("hitStageIndices");
            for (int v : arr) hitStageIndices.add(v);
        }
        coldStageHits = nbt.getInt("coldStageHits");
        warmStageHits = nbt.getInt("warmStageHits");
        hotStageHits = nbt.getInt("hotStageHits");
        veryHotStageHits = nbt.getInt("veryHotStageHits");
        nearMeltStageHits = nbt.getInt("nearMeltStageHits");
        moltenStageHits = nbt.getInt("moltenStageHits");

        // restore miss hits if stored in world nbt
        missMarkerHits = nbt.contains("missHits") ? nbt.getInt("missHits") : 0;

        fastMarkerIndices.clear();
        if (nbt.contains("fastMarkerIndices")) {
            int[] arr = nbt.getIntArray("fastMarkerIndices");
            for (int idx : arr) {
                fastMarkerIndices.add(idx);
            }
        }

        if (nbt.contains("morphProgress")) {
            morphProgress = nbt.getDouble("morphProgress");
        }

        // Removed restoration of continuous stage ticks.
    }

    public void restoreFromItemNbt(ItemStack stack) {
        if (!stack.isEmpty()) {
            NbtCompound itemNbt = stack.getOrCreateNbt();
            this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
            this.markerAttempts = itemNbt.getInt(ATTEMPTS_NBT_KEY);
            this.missMarkerHits = itemNbt.contains(MISS_MARKER_NBT_KEY) ? itemNbt.getInt(MISS_MARKER_NBT_KEY) : 0;

            // Restore temperature data from item if available
            if (itemNbt.contains("hitTemperatures") && hitTemperatures.isEmpty()) {
                int[] temps = itemNbt.getIntArray("hitTemperatures");
                for (int temp : temps) {
                    hitTemperatures.add(temp);
                }
                coldStageHits = itemNbt.getInt("coldStageHits");
                warmStageHits = itemNbt.getInt("warmStageHits");
                hotStageHits = itemNbt.getInt("hotStageHits");
                veryHotStageHits = itemNbt.getInt("veryHotStageHits");
                nearMeltStageHits = itemNbt.getInt("nearMeltStageHits");
                moltenStageHits = itemNbt.getInt("moltenStageHits");
            }

            // Restore per-hit stage indices if present
            if (itemNbt.contains("hitStageIndices")) {
                hitStageIndices.clear();
                int[] arr = itemNbt.getIntArray("hitStageIndices");
                for (int v : arr) hitStageIndices.add(v);
            }

            // Restore fast marker hits if present
            if (itemNbt.contains(FAST_MARKER_HITS_NBT_KEY)) {
                fastMarkerHits = itemNbt.getInt(FAST_MARKER_HITS_NBT_KEY);
            }

            // Removed restoration of continuous stage tick counters.
        } else {
            this.markerHitsCount = 0;
            this.markerAttempts = 0;
            this.fastMarkerHits = 0;
            this.missMarkerHits = 0;
            // Removed resets for continuous stage tick counters.
        }
    }

    public double getMorphProgress() {
        if (TOTAL_MARKERS <= 0) return 0.0;
        return Math.min(1.0, (double) markerHitsCount / TOTAL_MARKERS);
    }

    private void clearActiveMarker() {
        markerPositions.clear();
        markerHits.clear();
    }

    private boolean isMorphingActive(ItemStack stack) {
        return stack.getItem() instanceof MorphedItem && MorphedItem.getMorphProgress(stack) < 1.0;
    }

    private Vec2f nextMarkerPosition(ItemStack stackForMarker, MinigameCallback callback) {
        // For morphed items, we need to use the callback to access block entity methods
        // First, check if we can cast the callback to SmithingAnvilBlockEntity for morphed positioning
        if (stackForMarker.getItem() instanceof MorphedItem && callback instanceof com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity anvilEntity) {
            Vec2f marker = MinigamePositioning.getRandomMarkerPositionMorphed(anvilEntity);
            if (!marker.equals(Vec2f.ZERO)) {
                return marker;
            }
        }
        // Fallback to regular marker positioning
        return MinigamePositioning.getRandomMarkerPosition(stackForMarker, callback.getCachedState());
    }

    private void updateMorphProgressOnItem(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof MorphedItem) {
            stack.getOrCreateNbt().putDouble(MorphedItem.PROGRESS_KEY, getMorphProgress());
        }
    }

    private void refreshMorphImages(MinigameCallback callback) {
        World world = callback.getWorld();
        if (world != null && world.isClient) {
            ItemStack stack = callback.getCurrentStack();
            startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());
            // plannedProductImage would need to be set based on planned product if available
        }
    }
}
