package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.condition.ConditionLootTables;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
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
import net.minecraft.text.Text;
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

// TODO it seems its just randomly adding conditions not based on any loottable? Or maybe because there is not loottable for 10 hits currently. Probably better to register misshits and base the loottables around that.
// TODO adding already made items triggers : No schematics.

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
	@Setter
	private int markerAttempts = 0;
	@Setter
	private int markerHitsCount = 0;

	// Minigame Timing Variables
	private int markerTimeout = 0;
	private int markerSpawnDelay = 0;

	// Minigame Timing Constants - Made configurable
	public static final int INITIAL_MARKER_DELAY_TICKS = 25; // 0.5 seconds
	public static final int SUBSEQUENT_MARKER_DELAY_TICKS = 20; // 0.75 seconds
	public static final int MARKER_LIFETIME_TICKS_NORMAL = 35; // 1.5 seconds for normal markers
	public static final int MARKER_LIFETIME_TICKS_FAST = 20; // 0.75 seconds for fast markers

	private final Random random = new Random();

	private static final int ANVIL_INVENTORY_COOL_PER_TICK = 1;
	private static final int ANVIL_INVENTORY_COOL_TICK_INTERVAL = 20;
	private int anvilInventoryCoolTickCounter = 0;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
	private static final int TOTAL_MARKERS = 10;
	private static final int FAST_MARKERS = 5;

	private final List<Integer> fastMarkerIndices = new ArrayList<>();

	// Anvil specific constants for positioning. These are the fixed Y values for server-side particle spawning.
	// The renderer will calculate more precise values.
	private static final float ANVIL_TOP_Y = 0.9375f; // Max Y from anvil voxel shapes
	private static final float Y_FIGHTING_OFFSET = 0.001f; // Small offset to prevent z-fighting
	// This offset positions particles and debug visuals slightly above the item's surface.
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

	// One-shot client overlay to show the final fully-morphed texture
	private transient boolean showFinalMorphOnce = false; // client-only, not persisted
	private boolean pendingFinalMorphNotify = false;      // server-side signal for clients

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
		this.markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
	}

	// =================================
	// Main Interaction Logic
	// =================================

	public ActionResult onHammerHit(PlayerEntity player, BlockHitResult hitResult) {
		if (world == null || world.isClient) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = getInventory().getStack(0);
		// Block hammer interaction if recipe is finished and result item is present
		if (!anvilItem.isEmpty() && !ingotCrafting && plannedProductId == null) {
			player.sendMessage(net.minecraft.text.Text.literal("That is done!"), true);
			return ActionResult.FAIL;
		}

		// Require schematic selection for ingot-crafting
		if (ingotCrafting && plannedProductId == null) {
			openSchematicSelection(player);
			return ActionResult.FAIL;
		}

		int temp = TemperatureUtils.getTemperature(anvilItem);

		// Temperature gate
		if (temp < 0) {
			player.sendMessage(Text.literal("The material is too cold to work!"), true);
			world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 1.0f);
			playMissEffect();
			return ActionResult.FAIL;
		}

		Vec2f offsetVec;
		if (ingotCrafting && plannedProductId != null) {
			offsetVec = MinigamePositioningUtil.getMorphedTextureOffsetVec2f(this);
		} else {
			offsetVec = MinigamePositioningUtil.getItemTextureOffsetVec2f(anvilItem);
		}
		Vec2f itemLocalHit = MinigamePositioningUtil.worldHitToItemLocal(
				hitResult, getCachedState(), offsetVec
		);

		boolean hit = false;
		if (markerPositions.size() == 1) { // Only check if a marker is active
			Vec2f marker = markerPositions.get(0);
			// The visual marker has a half-width of 0.035. The squared distance to the corner is 2 * (0.035^2) = 0.00245.
			// We use a slightly larger radius to be more forgiving.
			double distSq = marker.distanceSquared(itemLocalHit);
			if (distSq < 0.0085f) {
				setMarkerHit(0); // This calls playHitEffect and handles particle/sound
				hit = true;
			}
		}

		if (!hit) {
			playMissEffect();
		}
		processMarkerAttempt(hit);

		if (markerHitsCount >= TOTAL_MARKERS) {
			applySmithingResult();
			resetMarkerProgress();
		}
		return ActionResult.SUCCESS;
	}

	public boolean isGuiBlocked(World world) {
		return world != null && world.getTime() < guiBlockCooldownUntil;
	}

	public ActionResult tryPickupItem(PlayerEntity player) {
		if (world == null || world.isClient) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = getInventory().getStack(0);
		if (!anvilItem.isEmpty()) {
			if (ingotCrafting && plannedProductId != null) {
				player.sendMessage(Text.literal("That needs to be finished first!"), true);
				return ActionResult.SUCCESS;
			}
			saveProgressToItem();
			player.getInventory().offerOrDrop(anvilItem.copy());
			getInventory().setStack(0, ItemStack.EMPTY);
			// Clear ingot mode state
			ingotCrafting = false;
			plannedProductId = null;
			markDirty();
			resetMarkers();
			guiBlockCooldownUntil = world.getTime() + 20; // Block GUI for 1 second
		}
		return ActionResult.SUCCESS;
	}

	public ActionResult tryPlaceItem(PlayerEntity player, Hand hand) {
		if (world == null || world.isClient) {
			return ActionResult.SUCCESS;
		}
		ItemStack stackInHand = player.getStackInHand(hand);
		ItemStack anvilItem = getInventory().getStack(0);

		if (anvilItem.isEmpty()) {
			// Only allow items with forgero:max_temperature attribute
			if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(stackInHand)) {
				ItemStack toPlace = stackInHand.copy();
				toPlace.setCount(1);
				getInventory().setStack(0, toPlace);
				stackInHand.decrement(1);

				// Enter ingot-crafting mode and wait for schematic selection on hammer hit
				ingotCrafting = true;
				plannedProductId = null;
				resetMarkerProgress();
				markDirty();
				// GUI will open on first hammer hit, not here
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
		if (world == null || world.isClient) {
			super.markDirty();
			return;
		}
		super.markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		// Manual sync for marker and progress data
		syncCustomDataToClients();
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

		nbt.putIntArray("fastMarkerIndices", fastMarkerIndices.stream().mapToInt(Integer::intValue).toArray());
		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
			itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		}

		// Ingot crafting state
		nbt.putBoolean("ingotCrafting", ingotCrafting);
		if (plannedProductId != null) {
			nbt.putString("plannedProductId", plannedProductId.toString());
		}
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
		// --- Fetch starting item image and planned product image ---
		if (world != null && world.isClient) {
			ItemStack stack = getInventory().getStack(0);
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
		if (markerHitsCount >= TOTAL_MARKERS) return;
		markerAttempts++;
		ItemStack stack = simpleInventory.getStack(0);
		int temp = TemperatureUtils.getTemperature(stack);
		int maxTemp = TemperatureUtils.getMaxTemp(stack);
		int depletion = hit ? 0 : 10;
		if (hit) {
			markerHitsCount++;
			// Determine if this is a fast marker
			boolean isFastMarker = fastMarkerIndices.contains(markerAttempts - 1); // markerAttempts is incremented above
			int tempIncrease = isFastMarker ? 50 : 30;
			int newTemp = Math.min(maxTemp, temp + tempIncrease);
			TemperatureUtils.setTemperature(stack, newTemp);
		} else {
			int newTemp = Math.max(TemperatureUtils.MIN_TEMPERATURE, temp - depletion);
			TemperatureUtils.setTemperature(stack, newTemp);
		}
		markDirty();
		markerPositions.clear(); // Clear existing marker to wait for next spawn
		markerHits.clear(); // Clear existing marker hit status

		markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS; // Always set delay for next marker
	}

	private void applySmithingResult() {
		ItemStack anvilItem = getInventory().getStack(0);
		if (anvilItem.isEmpty() || world == null) {
			return;
		}

		int temp = TemperatureUtils.getTemperature(anvilItem); // Always get starting item temperature

		// Only ingot-crafting path remains
		if (ingotCrafting && plannedProductId != null) {
			ItemStack newProduct = createProductFromPlanned(plannedProductId);
			if (!newProduct.isEmpty()) {
				// --- Copy temperature from original item to result item ---
				TemperatureUtils.setTemperature(newProduct, temp);
				// The colormap is determined by temperature, so this ensures the result item uses the same colormap.

				// --- Apply condition to ingot-crafted tool ---
				if (markerHitsCount >= 3) {
					var stateOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert(newProduct);
					if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?>) {
						var state = stateOpt.get();
						com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional = (com.sigmundgranaas.forgero.core.condition.Conditional<?>) stateOpt.get();
						if (state instanceof com.sigmundgranaas.forgero.core.state.Typed) {
							com.sigmundgranaas.forgero.core.state.Typed typed = (com.sigmundgranaas.forgero.core.state.Typed) state;
							if (TemperatureUtils.hasMaxTemperature(newProduct)) {
								LOGGER.info("applySmithingResult: Toolpart found in newProduct: {}", newProduct);
								int hits = markerHitsCount;
								java.util.List<com.sigmundgranaas.forgero.core.condition.NamedCondition> lootTable;
								if (hits == 3) {
									lootTable = ConditionLootTables.BEST;
								} else if (hits == 2) {
									lootTable = ConditionLootTables.GOOD;
								} else if (hits == 1) {
									lootTable = ConditionLootTables.NEUTRAL;
								} else if (hits == 0) {
									lootTable = ConditionLootTables.BAD;
								} else {
									lootTable = com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.all().stream()
											.filter(c -> c instanceof com.sigmundgranaas.forgero.core.condition.NamedCondition)
											.map(c -> (com.sigmundgranaas.forgero.core.condition.NamedCondition) c)
											.collect(java.util.stream.Collectors.toList());
								}
								if (!lootTable.isEmpty()) {
									var randomCondition = ConditionLootTables.getRandomCondition(lootTable);
									LOGGER.info("applySmithingResult: Applying loot table condition: {}", randomCondition.name());
									var conditioned = conditional.applyCondition(randomCondition);
									var newStackOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State) conditioned);
									if (newStackOpt.isPresent()) {
										newProduct = newStackOpt.get();
										LOGGER.info("applySmithingResult: Condition applied to ingot-crafted tool");
										// Ensure temperature is still set after condition application
										TemperatureUtils.setTemperature(newProduct, temp);
									}
								} else {
									LOGGER.info("applySmithingResult: No conditions available to apply");
								}
							}
						}
					}
				}
				getInventory().setStack(0, newProduct);
				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
			} else {
				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 0.8f);
			}
			ingotCrafting = false;
			plannedProductId = null;

			// Notify clients to show a one-frame fully morphed overlay
			pendingFinalMorphNotify = true;

			markDirty();
			return;
		}
	}

	public void setMarkerHit(int index) {
		if (world != null && !world.isClient && index >= 0 && index < markerHits.size()) {
			markerHits.set(index, true);
			playHitEffect(markerPositions.get(index)); // Trigger hit effect with position
			markerPositions.clear(); // Clear marker immediately on hit
			markerHits.clear();
			markDirty();
		}
	}

	private void playHitEffect(Vec2f markerLocalPos) {
		if (world instanceof ServerWorld serverWorld) {
			// Use a fixed Y for server-side particle spawning. Client will handle precise Y.
			float particleY = ANVIL_TOP_Y + Y_FIGHTING_OFFSET + MARKER_VISUAL_Y_OFFSET;

			Vec2f offsetVec = (ingotCrafting && plannedProductId != null)
					? MinigamePositioningUtil.getMorphedTextureOffsetVec2f(this)
					: MinigamePositioningUtil.getItemTextureOffsetVec2f(getInventory().getStack(0));

			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioningUtil.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);

			// Distinct particle for hit
			// Reduced spread and speed for smaller particles
			serverWorld.spawnParticles(ParticleTypes.FLAME, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 4, 0.001, 0.001, 0.001, 0.05);
			serverWorld.spawnParticles(ParticleTypes.LAVA, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 2, 0.01, 0.01, 0.01, 0.02);
			// Distinct sound for hit
			serverWorld.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
		}
	}

	private void playMissEffect() {
		if (world instanceof ServerWorld serverWorld) {
			// Distinct sound for miss
			serverWorld.playSound(null, getPos(), SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1.0f);
			// Distinct particle for miss (e.g., smoke)
			serverWorld.spawnParticles(ParticleTypes.SMOKE, getPos().getX() + 0.5, getPos().getY() + 1.0, getPos().getZ() + 0.5, 10, 0.3, 0.1, 0.3, 0.05);
		}
	}

	private void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack) {
		if (world instanceof ServerWorld serverWorld) {
			// Use a fixed Y for server-side particle spawning. Client will handle precise Y.
			float particleY = ANVIL_TOP_Y + Y_FIGHTING_OFFSET + MARKER_VISUAL_Y_OFFSET;

			Vec2f offsetVec = (ingotCrafting && plannedProductId != null)
					? MinigamePositioningUtil.getMorphedTextureOffsetVec2f(this)
					: MinigamePositioningUtil.getItemTextureOffsetVec2f(itemStack);

			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioningUtil.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);

			// Reduced spread and speed for smaller particles
			serverWorld.playSound(null, getPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.5f, 1.0f);
		}
	}


	public void resetMarkers() {
		resetMarkerProgress();
	}

	public void tick() {
		if (world == null || world.isClient) {
			return;
		}

		// --- Cooldown logic ---
		if (guiBlockCooldownUntil > 0 && world.getTime() >= guiBlockCooldownUntil) {
			guiBlockCooldownUntil = 0;
		}

		// --- Inventory cooling ---
		anvilInventoryCoolTickCounter++;
		if (anvilInventoryCoolTickCounter >= ANVIL_INVENTORY_COOL_TICK_INTERVAL) {
			anvilInventoryCoolTickCounter = 0;
			ItemStack stack = simpleInventory.getStack(0);
			if (!stack.isEmpty()) {
				int temp = TemperatureUtils.getTemperature(stack);
				if (temp > 20) {
					temp = Math.max(20, temp - ANVIL_INVENTORY_COOL_PER_TICK);
					TemperatureUtils.setTemperature(stack, temp);
					markDirty();
				}
			}
		}

		// --- Marker spawn logic ---
		ItemStack stackForMarker = simpleInventory.getStack(0);
		if (stackForMarker.isEmpty() || markerHitsCount >= TOTAL_MARKERS) {
			if (!markerPositions.isEmpty() || markerSpawnDelay > 0) {
				clearMarkerProgress();
				markDirty();
			}
			return;
		}

		// Require a selected mold before spawning markers
		if (ingotCrafting && plannedProductId == null) {
			// Keep progress cleared until user selects a mold
			if (!markerPositions.isEmpty() || markerSpawnDelay != INITIAL_MARKER_DELAY_TICKS) {
				clearMarkerProgress();
				markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
				markDirty();
			}
			return;
		}

		int temp = TemperatureUtils.getTemperature(stackForMarker);
		int maxTemp = TemperatureUtils.getMaxTemp(stackForMarker);
		boolean inStage;
		// For ingots, bypass stage gating to allow immediate play after selection
		if (ingotCrafting) {
			inStage = true;
		} else if (markerAttempts < 5) {
			inStage = TemperatureColorProvider.inFirstStageSmithing(temp, maxTemp);
		} else {
			inStage = TemperatureColorProvider.inSecondStageSmithing(temp, maxTemp);
		}

		if (inStage) {
			if (markerPositions.isEmpty()) {
				if (markerSpawnDelay > 0) {
					markerSpawnDelay--;
				}
				if (markerSpawnDelay == 0) {
					Vec2f marker;
					if (ingotCrafting && plannedProductId != null) {
						marker = MinigamePositioningUtil.getRandomMarkerPositionMorphed(this);
						if (marker.equals(Vec2f.ZERO)) {
							marker = MinigamePositioningUtil.getRandomMarkerPosition(stackForMarker, getCachedState());
						}
					} else {
						marker = MinigamePositioningUtil.getRandomMarkerPosition(stackForMarker, getCachedState());
					}
					if (marker.equals(Vec2f.ZERO)) {
						markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
						return;
					}
					markerPositions.add(marker);
					markerHits.add(false);
					markerTimeout = fastMarkerIndices.contains(markerAttempts) ? MARKER_LIFETIME_TICKS_FAST : MARKER_LIFETIME_TICKS_NORMAL;
					markDirty();
					spawnMarkerAppearanceEffect(marker, stackForMarker);
				}
			} else {
				markerTimeout--;
				if (markerTimeout <= 0) {
					processMarkerAttempt(false);
				}
			}
		} else {
			if (!markerPositions.isEmpty() || markerSpawnDelay > 0) {
				clearMarkerProgress();
				markDirty();
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

	public void loadProgressFromItem() {
		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
			this.markerAttempts = itemNbt.getInt(ATTEMPTS_NBT_KEY);
			if (itemNbt.contains("fastMarkerIndices")) {
				int[] arr = itemNbt.getIntArray("fastMarkerIndices");
				if (arr.length > 0) {
					fastMarkerIndices.clear();
					for (int idx : arr) {
						fastMarkerIndices.add(idx);
					}
				} else {
					resetMarkerProgress();
				}
			} else {
				resetMarkerProgress();
			}
		} else {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			this.fastMarkerIndices.clear();
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
		// Reset the mini-game so it starts fresh after selection
		resetMarkerProgress();
		// --- Fetch planned product image ---
		if (world != null && world.isClient) {
			ItemStack plannedStack = createProductFromPlanned(productId);
			plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
		}
	}

	// --- Getters for cached images ---
	public BufferedImage getStartingItemImage() {
		return startingItemImage;
	}

	public BufferedImage getPlannedProductImage() {
		return plannedProductImage;
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

	// Detect a simple material name from the current workpiece on the anvil.
	// Works for common naming schemes like "iron_ingot", "ingot_copper"
	private String detectMaterialForStack(ItemStack stack) {
		if (stack.isEmpty()) return "";
		Identifier id = Registries.ITEM.getId(stack.getItem());
		String path = id.getPath();
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
		return "";
	}

	// =================================
	// Positioning Utility Class and client sync remain unchanged
	// =================================

	// Client-only setter used by S2C sync to reflect ingot crafting state without resetting markers/minigame.
	public void clientSyncIngotState(boolean ingotCrafting, @Nullable Identifier plannedProductId) {
		this.ingotCrafting = ingotCrafting;
		this.plannedProductId = plannedProductId;
	}

	// Client-only: trigger a one-frame overlay of the fully-morphed texture
	public void clientTriggerFinalMorphOnce() {
		this.showFinalMorphOnce = true;
	}
	public boolean isShowFinalMorphOnce() {
		return showFinalMorphOnce;
	}
	public void clearFinalMorphOnce() {
		this.showFinalMorphOnce = false;
	}

	/**
	 * Returns the morph progress as a value between 0.0 and 1.0.
	 * Used for texture interpolation between starting and result images.
	 */
	public double getMorphProgress() {
		return Math.min(1.0, Math.max(0.0, (double) markerHitsCount / TOTAL_MARKERS));
	}
}
