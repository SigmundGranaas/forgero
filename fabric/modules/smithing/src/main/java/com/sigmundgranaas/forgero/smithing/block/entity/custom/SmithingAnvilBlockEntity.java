package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.condition.PredicateConditionLootRegistry;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.MinigamePositioningUtil;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import com.sigmundgranaas.forgero.smithing.util.SchematicResultUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// TODO isInStartingStage fails after 1200 max temp or something?

@Getter
public class SmithingAnvilBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroSmithingAnvil");
	private static final @NotNull String INVENTORY_NBT_KEY = "inventory";

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private final SimpleInventory simpleInventory = new SimpleInventory(inventory.size()) {
		@Override
		public ItemStack getStack(int slot) {
			return inventory.get(slot);
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			inventory.set(slot, stack);
			super.setStack(slot, stack);
		}
	};

	private List<Vec2f> markerPositions = new ArrayList<>();
	private List<Boolean> markerHits = new ArrayList<>();
	// Temperature tracking for hits
	private List<Integer> hitTemperatures = new ArrayList<>();
	private int redStageHits = 0;
	private int orangeStageHits = 0;
	private int yellowStageHits = 0;
	private int purpleStageHits = 0;
	private int strawStageHits = 0;
	private int blueStageHits = 0;
	private int brownStageHits = 0;
	private int greyStageHits = 0;

	@Setter
	private int markerAttempts = 0;
	@Setter
	private int markerHitsCount = 0;

	private int markerTimeout = 0;
	private int markerSpawnDelay = 0;

	public static final int INITIAL_MARKER_DELAY_TICKS = 25; // 0.5 seconds
	public static final int SUBSEQUENT_MARKER_DELAY_TICKS = 20; // 0.75 seconds
	public static final int MARKER_LIFETIME_TICKS_NORMAL = 35; // 1.5 seconds for normal markers
	public static final int MARKER_LIFETIME_TICKS_FAST = 20; // 0.75 seconds for fast markers

	private static final double MARKER_HIT_RADIUS_SQ = 0.0075d;

	private final Random random = new Random();

	private static final int ANVIL_INVENTORY_COOL_TICK_INTERVAL = 1; // Reduced from 20 to 5 ticks (0.25 seconds)
	private int anvilInventoryCoolTickCounter = 0;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
	private static final int TOTAL_MARKERS = 10;
	private static final int FAST_MARKERS = 5;

	private final List<Integer> fastMarkerIndices = new ArrayList<>();

	private static final float ANVIL_TOP_Y = 0.9375f;
	private static final float Y_FIGHTING_OFFSET = 0.001f;
	private static final float MARKER_VISUAL_Y_OFFSET = 0.01f;

	// Ingot-crafting mode
	private boolean ingotCrafting = false;
	@Nullable
	private Identifier plannedProductId = null;

	// Tag for any mod-provided ingots (c:ingots). Fallback heuristics are used if tags are missing.
	private static final TagKey<Item> INGOTS_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "ingots"));

	private long guiBlockCooldownUntil = 0;

	// Cached images for morphing/minigame
	private transient BufferedImage startingItemImage = null;
	private transient BufferedImage plannedProductImage = null;

	private transient boolean showFinalMorphOnce = false;
	private boolean pendingFinalMorphNotify = false;

	private double morphProgress = 0.0;

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
		this.markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
	}

	// =================================
	// Utilities
	// =================================

	private boolean isServer() {
		return world != null && !world.isClient;
	}

	private ItemStack currentStack() {
		return simpleInventory.getStack(0);
	}

	private Vec2f resolveOffsetVec(ItemStack stack) {
		boolean morphedOrPlanned = (ingotCrafting && plannedProductId != null) || stack.getItem() instanceof MorphedItem;
		return morphedOrPlanned
				? MinigamePositioningUtil.getMorphedTextureOffsetVec2f(this)
				: MinigamePositioningUtil.getItemTextureOffsetVec2f(stack);
	}

	private float serverParticleY() {
		return ANVIL_TOP_Y + Y_FIGHTING_OFFSET + MARKER_VISUAL_Y_OFFSET;
	}

	private boolean isMorphingActive(ItemStack stack) {
		return stack.getItem() instanceof MorphedItem && MorphedItem.getMorphProgress(stack) < 1.0;
	}

	private void clearActiveMarker() {
		markerPositions.clear();
		markerHits.clear();
	}

	private void resetCraftingState() {
		ingotCrafting = false;
		plannedProductId = null;
	}

	private Vec2f nextMarkerPosition(ItemStack stackForMarker) {
		Vec2f marker = MinigamePositioningUtil.getRandomMarkerPositionMorphed(this);
		if (marker.equals(Vec2f.ZERO)) {
			marker = MinigamePositioningUtil.getRandomMarkerPosition(stackForMarker, getCachedState());
		}
		return marker;
	}

	// =================================
	// Main Interaction Logic
	// =================================

	public ActionResult onHammerHit(PlayerEntity player, BlockHitResult hitResult) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = currentStack();
		if (anvilItem.isEmpty()) {
			playMissEffect();
			return ActionResult.FAIL;
		}

		// Temperature gating for morphed items as well
		if (anvilItem.getItem() instanceof MorphedItem) {
			int temperature = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getTemperature(anvilItem);
			int maxTemp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getMaxTemp(anvilItem);
			if (!com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp)) {
				player.sendMessage(net.minecraft.text.Text.of("That needs to be heaten up first!"), true);
				return ActionResult.FAIL;
			}
		}

		if (ingotCrafting && plannedProductId == null && !(anvilItem.getItem() instanceof MorphedItem)) {
			int temperature = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getTemperature(anvilItem);
			int maxTemp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getMaxTemp(anvilItem);
			if (!com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp)) {
				player.sendMessage(net.minecraft.text.Text.of("That needs to be heaten up first!"), true);
				return ActionResult.FAIL;
			}
			openSchematicSelection(player);
			return ActionResult.FAIL;
		}

		Vec2f offsetVec = resolveOffsetVec(anvilItem);
		Vec2f itemLocalHit = MinigamePositioningUtil.worldHitToItemLocal(hitResult, getCachedState(), offsetVec);

		boolean hit = false;
		if (markerPositions.size() == 1) {
			Vec2f marker = markerPositions.get(0);
			if (marker.distanceSquared(itemLocalHit) < MARKER_HIT_RADIUS_SQ) {
				setMarkerHit(0);
				hit = true;
			}
		}

		if (!hit) {
			playMissEffect();
		}
		processMarkerAttempt(hit);

		if (getMarkerHitsCount() >= TOTAL_MARKERS) {
			if (anvilItem.getItem() instanceof MorphedItem) {
				setMorphProgress(1.0);
				resetMarkerProgress();
			} else {
				resetMarkerProgress();
			}
		}
		return ActionResult.SUCCESS;
	}

	public boolean isGuiBlocked(World world) {
		return world != null && world.getTime() < guiBlockCooldownUntil;
	}

	public ActionResult tryPickupItem(PlayerEntity player) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = currentStack();
		if (!anvilItem.isEmpty()) {
			saveProgressToItem();
			player.getInventory().offerOrDrop(anvilItem.copy());
			getInventory().setStack(0, ItemStack.EMPTY);
			resetCraftingState();
			markDirty();
			resetMarkers();
			guiBlockCooldownUntil = world.getTime() + 20; // 1s
		}
		return ActionResult.SUCCESS;
	}

	public ActionResult tryPlaceItem(PlayerEntity player, Hand hand) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack stackInHand = player.getStackInHand(hand);
		ItemStack anvilItem = getInventory().getStack(0);

		if (anvilItem.isEmpty()) {
			// Accept any item with hasMaxTemperature OR MorphedItem
			if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(stackInHand)){
				ItemStack toPlace = stackInHand.copy();
				toPlace.setCount(1);
				getInventory().setStack(0, toPlace);

				ingotCrafting = !(stackInHand.getItem() instanceof MorphedItem);

				plannedProductId = null;
				stackInHand.decrement(1);

				if (stackInHand.getItem() instanceof MorphedItem) {
					// Restore progress from NBT for MorphedItem
					NbtCompound nbt = toPlace.getOrCreateNbt();
					this.markerHitsCount = nbt.getInt(HITS_NBT_KEY);
					this.markerAttempts = nbt.getInt(ATTEMPTS_NBT_KEY);
					this.fastMarkerIndices.clear();
					if (nbt.contains("fastMarkerIndices")) {
						int[] arr = nbt.getIntArray("fastMarkerIndices");
						for (int idx : arr) {
							this.fastMarkerIndices.add(idx);
						}
					}
					this.morphProgress = nbt.getDouble("morphProgress");
					// Do NOT reset marker progress, just clear active marker
					clearActiveMarker();
				} else {
					// Reset progress for new items
					resetMarkerProgress();
				}
				markDirty();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.SUCCESS;
	}


	// =================================
	// NBT and Syncing
	// =================================

	@Override
	public void markDirty() {
		super.markDirty();
		if (isServer()) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
			syncCustomDataToClients();
		}
	}

	@Override
	public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.inventory);

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
		nbt.putInt("redStageHits", redStageHits);
		nbt.putInt("orangeStageHits", orangeStageHits);
		nbt.putInt("yellowStageHits", yellowStageHits);
		nbt.putInt("purpleStageHits", purpleStageHits);
		nbt.putInt("strawStageHits", strawStageHits);
		nbt.putInt("blueStageHits", blueStageHits);
		nbt.putInt("brownStageHits", brownStageHits);
		nbt.putInt("greyStageHits", greyStageHits);

		nbt.putIntArray("fastMarkerIndices", fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray());
		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
			itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
			// Store temperature data on the item as well
			itemNbt.putIntArray("hitTemperatures", hitTemperatures.stream().mapToInt(Integer::intValue).toArray());
			itemNbt.putInt("redStageHits", redStageHits);
			itemNbt.putInt("orangeStageHits", orangeStageHits);
			itemNbt.putInt("yellowStageHits", yellowStageHits);
			itemNbt.putInt("purpleStageHits", purpleStageHits);
			itemNbt.putInt("strawStageHits", strawStageHits);
			itemNbt.putInt("blueStageHits", blueStageHits);
			itemNbt.putInt("brownStageHits", brownStageHits);
			itemNbt.putInt("greyStageHits", greyStageHits);
		}

		// Ingot crafting state
		nbt.putBoolean("ingotCrafting", ingotCrafting);
		if (plannedProductId != null) {
			nbt.putString("plannedProductId", plannedProductId.toString());
		}
		nbt.putDouble("morphProgress", morphProgress);
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, this.inventory);

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
		redStageHits = nbt.getInt("redStageHits");
		orangeStageHits = nbt.getInt("orangeStageHits");
		yellowStageHits = nbt.getInt("yellowStageHits");
		purpleStageHits = nbt.getInt("purpleStageHits");
		strawStageHits = nbt.getInt("strawStageHits");
		blueStageHits = nbt.getInt("blueStageHits");
		brownStageHits = nbt.getInt("brownStageHits");
		greyStageHits = nbt.getInt("greyStageHits");

		fastMarkerIndices.clear();
		if (nbt.contains("fastMarkerIndices")) {
			int[] arr = nbt.getIntArray("fastMarkerIndices");
			for (int idx : arr) {
				fastMarkerIndices.add(idx);
			}
		}

		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
			this.markerAttempts = itemNbt.getInt(ATTEMPTS_NBT_KEY);
			// Restore temperature data from item if available
			if (itemNbt.contains("hitTemperatures") && hitTemperatures.isEmpty()) {
				int[] temps = itemNbt.getIntArray("hitTemperatures");
				for (int temp : temps) {
					hitTemperatures.add(temp);
				}
				redStageHits = itemNbt.getInt("redStageHits");
				orangeStageHits = itemNbt.getInt("orangeStageHits");
				yellowStageHits = itemNbt.getInt("yellowStageHits");
				purpleStageHits = itemNbt.getInt("purpleStageHits");
				strawStageHits = itemNbt.getInt("strawStageHits");
				blueStageHits = itemNbt.getInt("blueStageHits");
				brownStageHits = itemNbt.getInt("brownStageHits");
				greyStageHits = itemNbt.getInt("greyStageHits");
			}
		} else {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
		}

		// Ingot crafting state
		this.ingotCrafting = nbt.getBoolean("ingotCrafting");
		if (nbt.contains("plannedProductId")) {
			try {
				this.plannedProductId = new Identifier(nbt.getString("plannedProductId"));
			} catch (Exception e) {
				this.plannedProductId = null;
			}
		} else {
			this.plannedProductId = null;
		}
		if (nbt.contains("morphProgress")) {
			morphProgress = nbt.getDouble("morphProgress");
		}
	}

	/**
	 * Sends custom sync data to all tracking clients.
	 * This is necessary for data that isn't handled by BlockEntityUpdateS2CPacket's default NBT sync.
	 */
	private void syncCustomDataToClients() {
		if (world == null || world.isClient) return;

		PacketByteBuf data = PacketByteBufs.create();
		data.writeBlockPos(getPos());

		// Inventory
		data.writeInt(simpleInventory.size());
		for (int i = 0; i < simpleInventory.size(); i++) {
			data.writeItemStack(simpleInventory.getStack(i));
		}

		// Marker positions and hits
		data.writeInt(markerPositions.size());
		for (int i = 0; i < markerPositions.size(); i++) {
			Vec2f pos = markerPositions.get(i);
			data.writeFloat(pos.x);
			data.writeFloat(pos.y);
			boolean hit = markerHits.size() > i && markerHits.get(i);
			data.writeBoolean(hit);
		}

		// Fast marker indices
		data.writeInt(fastMarkerIndices.size());
		for (int idx : fastMarkerIndices) {
			data.writeInt(idx);
		}

		// Progress counters
		data.writeInt(markerAttempts);
		data.writeInt(markerHitsCount);

		// Ingot crafting state
		data.writeBoolean(ingotCrafting);
		data.writeBoolean(plannedProductId != null);
		if (plannedProductId != null) {
			data.writeIdentifier(plannedProductId);
		}

		// One-shot final morph overlay notification
		data.writeBoolean(pendingFinalMorphNotify);

		for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}
		// Reset the pending flag after sending to all players
		pendingFinalMorphNotify = false;
	}


	// =================================
	// Minigame Logic
	// =================================

	public void resetMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();
		markerAttempts = 0;
		markerHitsCount = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
		fastMarkerIndices.clear();
		while (fastMarkerIndices.size() < FAST_MARKERS) {
			int idx = random.nextInt(TOTAL_MARKERS);
			if (!fastMarkerIndices.contains(idx)) {
				fastMarkerIndices.add(idx);
			}
		}
		// Only reset morph progress if not a MorphedItem with existing progress
		ItemStack stack = getInventory().getStack(0);
		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			NbtCompound nbt = stack.getOrCreateNbt();
			if (nbt.contains(HITS_NBT_KEY) || nbt.contains(ATTEMPTS_NBT_KEY)) {
				this.markerHitsCount = nbt.getInt(HITS_NBT_KEY);
				this.markerAttempts = nbt.getInt(ATTEMPTS_NBT_KEY);
				this.morphProgress = nbt.getDouble("morphProgress");
				// Do not reset morph progress, just clear markers
			} else {
				this.morphProgress = 0.0;
			}
		} else {
			this.morphProgress = 0.0;
		}
		updateMorphProgressOnItem();
		// --- Fetch starting item image and planned product image ---
		if (world != null && world.isClient) {
			startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());
			if (plannedProductId != null) {
				ItemStack plannedStack = createProductFromPlanned(plannedProductId);
				plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
			} else {
				plannedProductImage = null;
			}
		}
		markDirty();
	}

	public void clearMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();
		markerAttempts = 0;
		markerHitsCount = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS; // Reset to initial delay
		fastMarkerIndices.clear(); // Clear fast marker indices, will be regenerated on next start
	}

	public void processMarkerAttempt(boolean hit) {
		processMarkerAttempt(hit, true);
	}

	public void processMarkerAttempt(boolean hit, boolean wasPlayerAttempt) {
		if (markerHitsCount >= TOTAL_MARKERS) return;

		// Only increment attempts if this was an actual player attempt, not a timeout
		if (wasPlayerAttempt) {
			markerAttempts++;
		}

		ItemStack stack = simpleInventory.getStack(0);
		if (hit) {
			markerHitsCount++;
			// Track temperature at the time of successful hit
			int temperature = TemperatureUtils.getTemperature(stack);
			int maxTemp = TemperatureUtils.getMaxTemp(stack);
			// Add 30 temperature on successful hit
			TemperatureUtils.setTemperature(stack, Math.min(temperature + 30, maxTemp));
			hitTemperatures.add(TemperatureUtils.getTemperature(stack));

			// Update temperature stage counters using TemperatureColorProvider
			if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInRedStage(temperature, maxTemp)) {
				redStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInOrangeStage(temperature, maxTemp)) {
				orangeStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInYellowStage(temperature, maxTemp)) {
				yellowStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInPurpleStage(temperature, maxTemp)) {
				purpleStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInStrawStage(temperature, maxTemp)) {
				strawStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInBlueStage(temperature, maxTemp)) {
				blueStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInBrownStage(temperature, maxTemp)) {
				brownStageHits++;
			} else if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isInGreyStage(temperature, maxTemp)) {
				greyStageHits++;
			}

		} else if (wasPlayerAttempt) {
			// Only reduce temperature on actual player misses, not timeouts
			// Remove 15 temperature on misshit
			int temperature = TemperatureUtils.getTemperature(stack);
			int minTemp = 0;
			TemperatureUtils.setTemperature(stack, Math.max(temperature - 15, minTemp));
		}
		updateMorphProgressOnItem();
		markDirty();
		clearActiveMarker(); // wait for next spawn
		markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
	}

	public void setMarkerHit(int index) {
		if (world != null && !world.isClient && index >= 0 && index < markerHits.size()) {
			markerHits.set(index, true);
			playHitEffect(markerPositions.get(index));
			clearActiveMarker();
			markDirty();
		}
	}

	private void playHitEffect(Vec2f markerLocalPos) {
		if (world instanceof ServerWorld serverWorld) {
			float particleY = serverParticleY();
			Vec2f offsetVec = resolveOffsetVec(currentStack());
			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioningUtil.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);
			serverWorld.spawnParticles(ParticleTypes.FLAME, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 4, 0.001, 0.001, 0.001, 0.05);
			serverWorld.spawnParticles(ParticleTypes.LAVA, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 2, 0.01, 0.01, 0.01, 0.02);
			serverWorld.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
		}
	}

	private void playMissEffect() {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, getPos(), SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1.0f);
			serverWorld.spawnParticles(ParticleTypes.SMOKE, getPos().getX() + 0.5, getPos().getY() + 1.0, getPos().getZ() + 0.5, 10, 0.3, 0.1, 0.3, 0.05);
		}
	}

	private void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack) {
		if (world instanceof ServerWorld serverWorld) {
			float particleY = serverParticleY();
			Vec2f offsetVec = resolveOffsetVec(itemStack);
			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioningUtil.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);
			serverWorld.playSound(null, getPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.5f, 1.0f);
		}
	}

	public void resetMarkers() {
		resetMarkerProgress();
	}

	public void tick() {
		if (!isServer()) {
			return;
		}

		// GUI cooldown
		if (guiBlockCooldownUntil > 0 && world.getTime() >= guiBlockCooldownUntil) {
			guiBlockCooldownUntil = 0;
		}

		// Periodic dirty to ensure cooling/etc. visuals
		anvilInventoryCoolTickCounter++;
		if (anvilInventoryCoolTickCounter >= ANVIL_INVENTORY_COOL_TICK_INTERVAL) {
			anvilInventoryCoolTickCounter = 0;
			ItemStack stack = currentStack();
			if (!stack.isEmpty() && (stack.getItem() instanceof com.sigmundgranaas.forgero.minecraft.common.item.StateItem || stack.getItem() instanceof com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem)) {
				if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(stack)) {
					int temp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getTemperature(stack);
					int prevTemp = temp;
					if (temp > 20) {
						temp = Math.max(20, temp - 1); // Cool by 1 per interval
						com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.setTemperature(stack, temp);
					}
				}
			}
			markDirty();
		}

		ItemStack stackForMarker = currentStack();
		boolean activeMorph = isMorphingActive(stackForMarker);

		// Only run minigame if MorphedItem and morph not complete
		if (!activeMorph || stackForMarker.isEmpty()) {
			if (!markerPositions.isEmpty() || markerSpawnDelay > 0) {
				clearMarkerProgress();
				markDirty();
			}
			return;
		}

		// Temperature gating for marker spawning
		int temperature = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getTemperature(stackForMarker);
		int maxTemp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getMaxTemp(stackForMarker);
		boolean hotEnoughForWork = com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp);
		if (!hotEnoughForWork) {
			// Do not spawn markers if not hot enough
			return;
		}

		// Marker lifecycle
		if (markerPositions.isEmpty()) {
			if (markerSpawnDelay > 0) {
				markerSpawnDelay--;
			}
			if (markerSpawnDelay == 0) {
				Vec2f marker = nextMarkerPosition(stackForMarker);
				if (marker.equals(Vec2f.ZERO)) {
					markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
					return;
				}
				markerPositions.add(marker);
				markerHits.add(false);
				markerTimeout = fastMarkerIndices.contains(markerAttempts)
						? MARKER_LIFETIME_TICKS_FAST
						: MARKER_LIFETIME_TICKS_NORMAL;
				markDirty();
				spawnMarkerAppearanceEffect(marker, stackForMarker);
			}
		} else {
			markerTimeout--;
			if (markerTimeout <= 0) {
				processMarkerAttempt(false, false); // timeout is not a player attempt
			}
		}
	}


	// =================================
	// Data Persistence on Item
	// =================================

	public void saveProgressToItem() {
		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
			itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
			itemNbt.putIntArray("fastMarkerIndices", fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray());
		}
	}

	public SimpleInventory getInventory() {
		return simpleInventory;
	}

	// ---------------------------------
	// Schematic selection helpers / API
	// ---------------------------------

	public void openSchematicSelection(PlayerEntity player) {
		if (world == null || world.isClient) return;
		List<Identifier> options = SchematicResultUtil.findAvailableSchematicProductsForPlayer(player)
				.stream()
				.filter(id -> !createProductFromPlanned(id).isEmpty())
				.collect(Collectors.toList());
		SchematicResultUtil.openSchematicSelection(player, getPos(), options, world);
	}

	public void setPlannedProduct(Identifier productId) {
		this.plannedProductId = productId;
		markDirty();
		// Replace the ingot with the MorphedItem now that a result has been chosen
		replaceIngotWithMorphed();
		// Reset the mini-game so it starts fresh after selection
		resetMarkerProgress();
		// --- Fetch planned product image ---
		if (world != null && world.isClient) {
			ItemStack plannedStack = createProductFromPlanned(productId);
			plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
		}
	}

	// Client-only: refresh morph images based on current inventory and planned product
	public void clientRefreshMorphImages() {
		if (world == null || !world.isClient) return;
		ItemStack stack = getInventory().getStack(0);
		this.startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());
		if (plannedProductId != null) {
			ItemStack plannedStack = createProductFromPlanned(plannedProductId);
			this.plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
		} else {
			this.plannedProductImage = null;
		}
	}

	// Make product creation accessible to positioning util
	public ItemStack createProductFromPlanned(Identifier productId) {
		// Prefer resolving via StateService if your tool heads are states
		try {
			var maybeState = StateService.INSTANCE.find(productId.toString());
			if (maybeState.isPresent()) {
				var state = maybeState.get();
				var stackOpt = StateService.INSTANCE.convert(state);
				if (stackOpt.isPresent()) {
					return stackOpt.get();
				}
			}
		} catch (Throwable ignored) {
			// Fall through to try more specific materialized IDs
		}

		// Materialize the product for the current workpiece material (e.g., iron, copper, gold, etc.)
		String material = detectMaterialForStack(getInventory().getStack(0));
		List<Identifier> candidates = new ArrayList<>();
		if (material != null && !material.isEmpty()) {
			candidates.add(new Identifier(productId.getNamespace(), material + "_" + productId.getPath()));
			candidates.add(new Identifier(productId.getNamespace(), material + "-" + productId.getPath()));
		}
		candidates.add(productId);

		// Try states first
		for (Identifier id : candidates) {
			try {
				var maybeState = StateService.INSTANCE.find(id.toString());
				if (maybeState.isPresent()) {
					var stackOpt = StateService.INSTANCE.convert(maybeState.get());
					if (stackOpt.isPresent() && !stackOpt.get().isEmpty()) {
						return stackOpt.get();
					}
				}
			} catch (Throwable ignored) {
			}
		}

		// Fallback: registry items
		for (Identifier id : candidates) {
			try {
				var itemOpt = Registries.ITEM.getOrEmpty(id);
				if (itemOpt.isPresent()) {
					return new ItemStack(itemOpt.get());
				}
			} catch (Throwable ignored) {
			}
		}
		return ItemStack.EMPTY;
	}

	// Accepts any ingot by tag or simple name heuristic
	public boolean isIngot(ItemStack stack) {
		if (stack.isEmpty()) return false;
		try {
			if (stack.isIn(INGOTS_TAG)) return true;
		} catch (Throwable ignored) {
		}
		Identifier id = Registries.ITEM.getId(stack.getItem());
		String path = id.getPath();
		// Common patterns: copper_ingot, iron_ingot, netherite_ingot, ingot_copper
		return path.endsWith("_ingot") || path.startsWith("ingot_") || stack.isOf(Items.IRON_INGOT);
	}

	private String detectMaterialForStack(ItemStack stack) {
		if (stack.isEmpty()) return "";
		Identifier id = Registries.ITEM.getId(stack.getItem());
		String path = id.getPath();
		if (isIngot(stack)) {
			if (path.endsWith("_ingot")) {
				return path.substring(0, path.length() - "_ingot".length());
			}
			if (path.startsWith("ingot_") && path.length() > "ingot_".length()) {
				return path.substring("ingot_".length());
			}
			// Fallbacks: known vanilla special cases
			if (stack.isOf(Items.IRON_INGOT)) return "iron";
			if (stack.isOf(Items.GOLD_INGOT)) return "gold";
			if (stack.isOf(Items.COPPER_INGOT)) return "copper";
			if (stack.isOf(Items.NETHERITE_INGOT)) return "netherite";
			return path;
		} else {
			// For non-ingots, use the full item name
			return path;
		}
	}

	// Client-only setter used by S2C sync to reflect ingot crafting state without resetting markers/minigame.
	public void clientSyncIngotState(boolean ingotCrafting, @Nullable Identifier plannedProductId) {
		this.ingotCrafting = ingotCrafting;
		this.plannedProductId = plannedProductId;
	}

	// Client-only: trigger a one-frame overlay of the fully-morphed texture
	public void clientTriggerFinalMorphOnce() {
		this.showFinalMorphOnce = true;
	}

	private void replaceIngotWithMorphed() {
		if (world == null || world.isClient) return;
		if (plannedProductId == null) return;

		ItemStack current = getInventory().getStack(0);
		if (current.isEmpty()) return;

		Item morphedItem = findMorphedItem();
		if (morphedItem == null) {
			return;
		}

		ItemStack morphed = new ItemStack(morphedItem, 1);

		// NBT no longer needed - predicate system handles nether detection automatically

		// Initialize morph NBT (start -> ingot id, result -> selected product item id)
		ItemStack resultStack = createProductFromPlanned(plannedProductId);
		if (!resultStack.isEmpty()) {
			MorphedItem.setResultItem(morphed, resultStack.getItem());
		} else {
			// Fallback: store the planned id string if we couldn't materialize an item stack
			morphed.getOrCreateNbt().putString(MorphedItem.RESULT_KEY, plannedProductId.toString());
		}
		MorphedItem.setStartItem(morphed, current.getItem());
		MorphedItem.setMorphProgress(morphed, 0.0);

		TemperatureUtils.setMaxTemperature(morphed, TemperatureUtils.getMaxTemp(current));
		TemperatureUtils.setTemperature(morphed, TemperatureUtils.getTemperature(current));

		getInventory().setStack(0, morphed);
		// Keep ingotCrafting true so minigame continues to work with morphed item
		markDirty();
	}

	// Scan registry to find the MorphedItem instance registered by the mod
	@Nullable
	private Item findMorphedItem() {
		for (Item item : Registries.ITEM) {
			if (item instanceof MorphedItem) {
				return item;
			}
		}
		return null;
	}

	// Synchronize the current minigame progress to the MorphedItem stack's NBT
	private void updateMorphProgressOnItem() {
		ItemStack stack = simpleInventory.getStack(0);
		if (stack.isEmpty()) return;
		if (stack.getItem() instanceof MorphedItem) {
			stack.getOrCreateNbt().putDouble(MorphedItem.PROGRESS_KEY, getMorphProgress());
		}
	}


	public double getMorphProgress() {
		if (TOTAL_MARKERS <= 0) return 0.0;
		return Math.min(1.0, (double) markerHitsCount / TOTAL_MARKERS);
	}

	public void setMorphProgress(double progress) {
		this.morphProgress = progress;
		if (progress >= 1.0) {
			ItemStack stack = getInventory().getStack(0);
			if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
				Item resultItem = MorphedItem.getResultItem(stack);
				if (resultItem != null) {
					ItemStack resultStack = new ItemStack(resultItem, stack.getCount());
					// --- Apply condition to result item of morphed item using predicates ---
					var stateOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert(resultStack);
					if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?>) {
						var state = stateOpt.get();
						com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional = (com.sigmundgranaas.forgero.core.condition.Conditional<?>) stateOpt.get();

						// Create MatchContext for predicate evaluation
						MatchContext context = MatchContext.of();
						if (world != null) {
							context = context.put(MinecraftContextKeys.WORLD, world);
							context = context.put(MinecraftContextKeys.BLOCK_TARGET, pos);
						}
						// Add the ItemStack to context for potential predicate use
						context = context.put(MinecraftContextKeys.STACK, stack);

						// Add temperature tracking data to context
						context = context.put(MinecraftContextKeys.RED_STAGE_HITS, redStageHits);
						context = context.put(MinecraftContextKeys.ORANGE_STAGE_HITS, orangeStageHits);
						context = context.put(MinecraftContextKeys.YELLOW_STAGE_HITS, yellowStageHits);
						context = context.put(MinecraftContextKeys.PURPLE_STAGE_HITS, purpleStageHits);
						context = context.put(MinecraftContextKeys.STRAW_STAGE_HITS, strawStageHits);
						context = context.put(MinecraftContextKeys.BLUE_STAGE_HITS, blueStageHits);
						context = context.put(MinecraftContextKeys.BROWN_STAGE_HITS, brownStageHits);
						context = context.put(MinecraftContextKeys.GREY_STAGE_HITS, greyStageHits);
						context = context.put(MinecraftContextKeys.TOTAL_HITS, markerHitsCount);

						// Add accuracy and performance tracking data to context
						context = context.put(MinecraftContextKeys.TOTAL_ATTEMPTS, markerAttempts);
						int missHits = markerAttempts - markerHitsCount;
						context = context.put(MinecraftContextKeys.MISS_HITS, missHits);
						double accuracyRate = markerAttempts > 0 ? (double) markerHitsCount / markerAttempts : 0.0;
						context = context.put(MinecraftContextKeys.ACCURACY_RATE, accuracyRate);

						// Check for direct condition assignment using predicates
						com.sigmundgranaas.forgero.core.condition.NamedCondition directCondition = PredicateConditionLootRegistry.getCondition(context);
						if (directCondition != null) {
							var conditioned = conditional.applyCondition(directCondition);
							var newStackOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State) conditioned);
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
									.collect(Collectors.toList());
							if (!applicableConditions.isEmpty()) {
								com.sigmundgranaas.forgero.core.condition.NamedCondition randomCondition = applicableConditions.get(new Random().nextInt(applicableConditions.size()));
								var conditioned = conditional.applyCondition(randomCondition);
								var newStackOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State) conditioned);
								if (newStackOpt.isPresent()) {
									resultStack = newStackOpt.get();
								}
							}
						}
					}
					// --- Copy temperature from morphed item to result item ---
					double currentTemp = TemperatureUtils.getTemperature(stack);
					TemperatureUtils.setTemperature(resultStack, (int) Math.round(currentTemp));
					getInventory().setStack(0, resultStack);
					if (world != null && !world.isClient) {
						world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
					}
				}
			}
		}
		markDirty();
	}
}
