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
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem;
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
	public static final int FAST_MARKERS = 3;

	private static final double MARKER_HIT_RADIUS_SQ = 0.0075d;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
	private static final String FAST_MARKER_HITS_NBT_KEY = "forgero_fastMarkerHits";
	private static final String MISS_MARKER_NBT_KEY = "forgero_missMarkerHits";

	private final List<Vec2f> markerPositions = new ArrayList<>();
	private final List<Boolean> markerHits = new ArrayList<>();
	private final List<Integer> hitStageIndices = new ArrayList<>();
	private final List<Integer> fastMarkerIndices = new ArrayList<>();
	private final Random random = new Random();

	private int fastMarkerHits = 0;
	private int missMarkerHits = 0;

	@Setter
	private int markerAttempts = 0;

	@Setter
	private int markerHitsCount = 0;

	private int markerTimeout = 0;
	private int markerSpawnDelay;

	private double morphProgress = 0.0;

	private transient BufferedImage startingItemImage = null;
	private transient BufferedImage plannedProductImage = null;

	private enum MarkerOutcome {
		HIT,
		MISS,
		TIMEOUT
	}

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
		fastMarkerHits = 0;
		missMarkerHits = 0;
		markerTimeout = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;

		fastMarkerIndices.clear();

		// Ensure first marker, index 0, is never a fast marker.
		while (fastMarkerIndices.size() < FAST_MARKERS) {
			int idx = 1 + random.nextInt(TOTAL_MARKERS - 1);

			if (!fastMarkerIndices.contains(idx)) {
				fastMarkerIndices.add(idx);
			}
		}

		ItemStack stack = callback.getCurrentStack();

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			NbtCompound nbt = stack.getOrCreateNbt();

			if (nbt.contains(HITS_NBT_KEY) || nbt.contains(ATTEMPTS_NBT_KEY)) {
				this.markerHitsCount = nbt.getInt(HITS_NBT_KEY);
				this.markerAttempts = this.markerHitsCount;
				this.morphProgress = nbt.getDouble("morphProgress");
				this.fastMarkerHits = nbt.getInt(FAST_MARKER_HITS_NBT_KEY);
				this.missMarkerHits = nbt.contains(MISS_MARKER_NBT_KEY)
						? nbt.getInt(MISS_MARKER_NBT_KEY)
						: 0;

				hitStageIndices.clear();

				if (nbt.contains("hitStageIndices")) {
					int[] arr = nbt.getIntArray("hitStageIndices");

					for (int v : arr) {
						hitStageIndices.add(v);
					}
				}

				if (nbt.contains("fastMarkerIndices")) {
					fastMarkerIndices.clear();

					int[] arr = nbt.getIntArray("fastMarkerIndices");

					for (int idx : arr) {
						fastMarkerIndices.add(idx);
					}
				}
			} else {
				this.morphProgress = 0.0;
				this.fastMarkerHits = 0;
				this.missMarkerHits = 0;
				hitStageIndices.clear();
			}
		} else {
			this.morphProgress = 0.0;
			this.fastMarkerHits = 0;
			this.missMarkerHits = 0;
			hitStageIndices.clear();
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
		fastMarkerHits = 0;
		missMarkerHits = 0;
		markerTimeout = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;

		fastMarkerIndices.clear();
		hitStageIndices.clear();
	}

	public boolean processHit(Vec2f itemLocalHit, MinigameCallback callback) {
		if (markerPositions.size() != 1) {
			return false;
		}

		Vec2f marker = markerPositions.get(0);
		return marker.distanceSquared(itemLocalHit) < MARKER_HIT_RADIUS_SQ;
	}

	public void processMarkerAttempt(boolean hit, MinigameCallback callback) {
		processMarkerAttempt(hit, true, callback);
	}

	public void processMarkerAttempt(boolean hit, boolean wasPlayerAttempt, MinigameCallback callback) {
		MarkerOutcome outcome = hit
				? MarkerOutcome.HIT
				: wasPlayerAttempt ? MarkerOutcome.MISS : MarkerOutcome.TIMEOUT;

		processMarkerAttempt(outcome, callback);
	}

	private void processMarkerAttempt(MarkerOutcome outcome, MinigameCallback callback) {
		if (isComplete()) {
			return;
		}

		ItemStack stack = callback.getCurrentStack();

		if (stack.isEmpty()) {
			clearActiveMarker();
			callback.markDirty();
			return;
		}

		switch (outcome) {
			case HIT -> {
				int markerIndex = markerHitsCount;
				applySuccessfulMarkerHit(markerIndex, stack, callback);
			}
			case MISS, TIMEOUT -> applyFailedMarkerAttempt();
		}

		clearActiveMarker();

		markerSpawnDelay = isComplete()
				? 0
				: SUBSEQUENT_MARKER_DELAY_TICKS;

		updateMorphProgressOnItem(stack);
		saveProgressToItem(stack);

		callback.markDirty();

		finishIfComplete(callback);
	}

	private void applySuccessfulMarkerHit(
			int markerIndex,
			ItemStack stack,
			MinigameCallback callback
	) {
		markerHitsCount++;

		/*
		 * markerAttempts now tracks successful marker progress only.
		 * Misses and timeouts do not advance this.
		 */
		markerAttempts = markerHitsCount;

		if (!markerPositions.isEmpty()) {
			markerHits.set(0, true);
			callback.playHitEffect(markerPositions.get(0));
		}

		int temperature = TemperatureUtils.getTemperature(stack);
		int maxTemp = TemperatureUtils.getMaxTemp(stack);
		int workableStart = TemperatureUtils.getWorkableTemperatureStart(stack);
		int workableEnd = TemperatureUtils.getWorkableTemperatureEnd(stack);

		hitStageIndices.add(stageIndexFor(
				temperature,
				maxTemp,
				workableStart,
				workableEnd
		));

		boolean fast = fastMarkerIndices.contains(markerIndex);
		int tempChange = fast ? -10 : 40;

		TemperatureUtils.setTemperature(
				stack,
				Math.max(0, Math.min(temperature + tempChange, maxTemp))
		);

		if (fast) {
			fastMarkerHits++;
		}
	}

	private void applyFailedMarkerAttempt() {
		/*
		 * Player misses and marker timeouts both count as failed attempts.
		 * This makes quality punishment consistent and prevents infinite retries.
		 */
		missMarkerHits++;
	}

	private void finishIfComplete(MinigameCallback callback) {
		if (!isComplete()) {
			return;
		}

		ItemStack stack = callback.getCurrentStack();

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			setMorphProgress(1.0, callback, null);
		}
	}

	private int stageIndexFor(int temperature, int maxTemp, int workableStart, int workableEnd) {
		DynamicTemperatureSystem.TemperatureStages stages =
				DynamicTemperatureSystem.calculateStages(maxTemp, workableStart, workableEnd);

		DynamicTemperatureSystem.TemperatureStage stage =
				DynamicTemperatureSystem.getStage(temperature, stages);

		return switch (stage) {
			case COLD -> 0;
			case WARM -> 1;
			case HOT -> 2;
			case WORKABLE -> 3;
			case OVERHEATED -> 4;
		};
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

		if (isComplete()) {
			finishIfComplete(callback);
			return;
		}

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

				markerTimeout = fastMarkerIndices.contains(markerHitsCount)
						? MARKER_LIFETIME_TICKS_FAST
						: MARKER_LIFETIME_TICKS_NORMAL;

				callback.markDirty();
				callback.spawnMarkerAppearanceEffect(marker, stackForMarker);
			}
		} else {
			markerTimeout--;

			if (markerTimeout <= 0) {
				processMarkerAttempt(false, false, callback);
			}
		}
	}

	public boolean isComplete() {
		return markerHitsCount >= TOTAL_MARKERS;
	}

	public void setMorphProgress(
			double progress,
			MinigameCallback callback,
			@SuppressWarnings("unused") Identifier plannedProductId
	) {
		this.morphProgress = progress;

		if (progress >= 1.0) {
			ItemStack stack = callback.getCurrentStack();

			if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
				Item resultItem = MorphedItem.getResultItem(stack);

				if (resultItem != null) {
					ItemStack resultStack = new ItemStack(resultItem, stack.getCount());

					var stateOpt = StateService.INSTANCE.convert(resultStack);

					if (stateOpt.isPresent()
							&& stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional) {
						var state = stateOpt.get();

						MatchContext context = createMatchContext(callback, stack);

						com.sigmundgranaas.forgero.core.condition.NamedCondition directCondition =
								PredicateConditionLootRegistry.getCondition(context);

						if (directCondition != null) {
							var conditioned = conditional.applyCondition(directCondition);

							var newStackOpt = StateService.INSTANCE.convert(
									(com.sigmundgranaas.forgero.core.state.State) conditioned
							);

							if (newStackOpt.isPresent()) {
								resultStack = newStackOpt.get();
							}
						} else {
							var lootTable = PredicateConditionLootRegistry.getLootTable(context);

							if (lootTable.isEmpty()) {
								lootTable = PredicateConditionLootRegistry.NEUTRAL;
							}

							List<com.sigmundgranaas.forgero.core.condition.NamedCondition> applicableConditions =
									lootTable.stream()
											.filter(cond -> cond.matches(state))
											.toList();

							if (!applicableConditions.isEmpty()) {
								com.sigmundgranaas.forgero.core.condition.NamedCondition randomCondition =
										applicableConditions.get(new Random().nextInt(applicableConditions.size()));

								var conditioned = conditional.applyCondition(randomCondition);

								var newStackOpt = StateService.INSTANCE.convert(
										(com.sigmundgranaas.forgero.core.state.State) conditioned
								);

								if (newStackOpt.isPresent()) {
									resultStack = newStackOpt.get();
								}
							}
						}
					}

					double currentTemp = TemperatureUtils.getTemperature(stack);
					TemperatureUtils.setTemperature(resultStack, (int) Math.round(currentTemp));

					callback.replaceWithResult(resultStack);

					World world = callback.getWorld();

					if (world != null && !world.isClient) {
						world.playSound(
								null,
								callback.getPos(),
								SoundEvents.BLOCK_ANVIL_USE,
								SoundCategory.BLOCKS,
								1.0f,
								1.0f
						);
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
		context = context.put(MinecraftContextKeys.TOTAL_HITS, markerHitsCount);
		context = context.put(MinecraftContextKeys.MISS_HITS, missMarkerHits);
		context = context.put(MinecraftContextKeys.FAST_MARKER_HITS, fastMarkerHits);

		return context;
	}

	public void saveProgressToItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (!(stack.getItem() instanceof MorphedItem)) {
			return;
		}

		updateMorphProgressOnItem(stack);

		NbtCompound itemNbt = stack.getOrCreateNbt();

		itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
		itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		itemNbt.putInt(MISS_MARKER_NBT_KEY, missMarkerHits);
		itemNbt.putInt(FAST_MARKER_HITS_NBT_KEY, fastMarkerHits);

		itemNbt.putIntArray(
				"fastMarkerIndices",
				fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray()
		);

		itemNbt.putIntArray(
				"hitStageIndices",
				hitStageIndices.stream().mapToInt(Integer::intValue).toArray()
		);

		itemNbt.putDouble("morphProgress", getMorphProgress());
	}
	
	public void writeNbt(NbtCompound nbt) {
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
		nbt.putInt(HITS_NBT_KEY, markerHitsCount);
		nbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		nbt.putInt(FAST_MARKER_HITS_NBT_KEY, fastMarkerHits);
		nbt.putInt(MISS_MARKER_NBT_KEY, missMarkerHits);
		nbt.putIntArray(
				"hitStageIndices",
				hitStageIndices.stream().mapToInt(Integer::intValue).toArray()
		);
		nbt.putIntArray(
				"fastMarkerIndices",
				fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray()
		);
		nbt.putDouble("morphProgress", morphProgress);
	}

	public void readNbt(NbtCompound nbt) {
		markerPositions.clear();
		markerHits.clear();

		if (nbt.contains("markers", NbtCompound.COMPOUND_TYPE)) {
			NbtCompound markersNbt = nbt.getCompound("markers");

			for (int i = 0; i < TOTAL_MARKERS; i++) {
				if (markersNbt.contains("marker_" + i)) {
					NbtCompound markerNbt = markersNbt.getCompound("marker_" + i);

					markerPositions.add(new Vec2f(
							markerNbt.getFloat("x"),
							markerNbt.getFloat("y")
					));
					markerHits.add(markerNbt.getBoolean("hit"));
				}
			}
		}

		markerHitsCount = nbt.contains(HITS_NBT_KEY)
				? nbt.getInt(HITS_NBT_KEY)
				: markerHitsCount;

		markerAttempts = markerHitsCount;

		fastMarkerHits = nbt.contains(FAST_MARKER_HITS_NBT_KEY)
				? nbt.getInt(FAST_MARKER_HITS_NBT_KEY)
				: fastMarkerHits;

		missMarkerHits = nbt.contains(MISS_MARKER_NBT_KEY)
				? nbt.getInt(MISS_MARKER_NBT_KEY)
				: nbt.contains("missHits") ? nbt.getInt("missHits") : 0;

		hitStageIndices.clear();

		if (nbt.contains("hitStageIndices")) {
			int[] arr = nbt.getIntArray("hitStageIndices");

			for (int v : arr) {
				hitStageIndices.add(v);
			}
		}

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
	}

	public void restoreFromItemNbt(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem)) {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			this.fastMarkerHits = 0;
			this.missMarkerHits = 0;
			this.morphProgress = 0.0;
			this.hitStageIndices.clear();
			this.fastMarkerIndices.clear();
			return;
		}

		if (!stack.hasNbt()) {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			this.fastMarkerHits = 0;
			this.missMarkerHits = 0;
			this.morphProgress = 0.0;
			this.hitStageIndices.clear();
			this.fastMarkerIndices.clear();
			return;
		}

		NbtCompound itemNbt = stack.getNbt();

		if (itemNbt == null) {
			return;
		}

		this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
		this.markerAttempts = this.markerHitsCount;

		this.missMarkerHits = itemNbt.contains(MISS_MARKER_NBT_KEY)
				? itemNbt.getInt(MISS_MARKER_NBT_KEY)
				: 0;

		hitStageIndices.clear();

		if (itemNbt.contains("hitStageIndices")) {
			int[] arr = itemNbt.getIntArray("hitStageIndices");

			for (int v : arr) {
				hitStageIndices.add(v);
			}
		}

		this.fastMarkerHits = itemNbt.contains(FAST_MARKER_HITS_NBT_KEY)
				? itemNbt.getInt(FAST_MARKER_HITS_NBT_KEY)
				: 0;

		fastMarkerIndices.clear();

		if (itemNbt.contains("fastMarkerIndices")) {
			int[] arr = itemNbt.getIntArray("fastMarkerIndices");

			for (int idx : arr) {
				fastMarkerIndices.add(idx);
			}
		}

		this.morphProgress = itemNbt.contains("morphProgress")
				? itemNbt.getDouble("morphProgress")
				: 0.0;
	}

	public double getMorphProgress() {
		if (TOTAL_MARKERS <= 0) {
			return 0.0;
		}

		return Math.min(1.0, (double) markerHitsCount / TOTAL_MARKERS);
	}

	private void clearActiveMarker() {
		markerPositions.clear();
		markerHits.clear();
		markerTimeout = 0;
	}

	private boolean isMorphingActive(ItemStack stack) {
		return stack.getItem() instanceof MorphedItem
				&& MorphedItem.getMorphProgress(stack) < 1.0;
	}

	private Vec2f nextMarkerPosition(ItemStack stackForMarker, MinigameCallback callback) {
		if (stackForMarker.getItem() instanceof MorphedItem
				&& callback instanceof com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity anvilEntity) {
			Vec2f marker = MinigamePositioning.getRandomMarkerPositionMorphed(anvilEntity);

			if (!marker.equals(Vec2f.ZERO)) {
				return marker;
			}
		}

		return MinigamePositioning.getRandomMarkerPosition(
				stackForMarker,
				callback.getCachedState()
		);
	}

	private void updateMorphProgressOnItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (stack.getItem() instanceof MorphedItem) {
			stack.getOrCreateNbt().putDouble(MorphedItem.PROGRESS_KEY, getMorphProgress());
		}
	}

	private void refreshMorphImages(MinigameCallback callback) {
		World world = callback.getWorld();

		if (world != null && world.isClient) {
			ItemStack stack = callback.getCurrentStack();
			startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(
					stack,
					MinecraftClient.getInstance()
			);

			// plannedProductImage would need to be set based on planned product if available.
		}
	}
}
