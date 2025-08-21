package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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
		if (anvilItem.isEmpty()) {
			playMissEffect();
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

		int[] offset = Positioning.getItemTextureOffset(anvilItem);
		Vec2f itemLocalHit = SmithingAnvilBlockEntity.Positioning.worldHitToItemLocal(hitResult, getCachedState(), new Vec2f(offset[0] / 16.0f, offset[1] / 16.0f));

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

		if (getMarkerAttempts() >= TOTAL_MARKERS) {
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
			// Accept any ingot (by tag or fallback heuristic)
			if (isIngot(stackInHand)) {
				ItemStack toPlace = stackInHand.copy();
				toPlace.setCount(1);
				getInventory().setStack(0, toPlace);
				// Initialize a working temperature so markers can start post-selection
				TemperatureUtils.setTemperature(toPlace, Math.max(TemperatureUtils.MIN_TEMPERATURE + 1, 100));
				stackInHand.decrement(1);

				// Enter ingot-crafting mode and prompt schematic selection
				ingotCrafting = true;
				plannedProductId = null;
				resetMarkerProgress();
				markDirty();
				openSchematicSelection(player);
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

		for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}
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
		if (markerAttempts >= TOTAL_MARKERS) return;
		markerAttempts++;
		ItemStack stack = simpleInventory.getStack(0);
		int temp = TemperatureUtils.getTemperature(stack);
		int depletion = hit ? 0 : 10;
		if (hit) {
			markerHitsCount++;
		}
		int newTemp = Math.max(TemperatureUtils.MIN_TEMPERATURE, temp - depletion);
		TemperatureUtils.setTemperature(stack, newTemp);
		markDirty();
		markerPositions.clear(); // Clear existing marker to wait for next spawn
		markerHits.clear(); // Clear existing marker hit status

		if (markerAttempts < TOTAL_MARKERS) {
			markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS; // Set delay for next marker
		}
	}

	private void applySmithingResult() {
		ItemStack anvilItem = getInventory().getStack(0);
		if (anvilItem.isEmpty() || world == null) {
			return;
		}

		// Only ingot-crafting path remains
		if (ingotCrafting && plannedProductId != null) {
			ItemStack newProduct = createProductFromPlanned(plannedProductId);
			if (!newProduct.isEmpty()) {
				int temp = TemperatureUtils.getTemperature(anvilItem);
				TemperatureUtils.setTemperature(newProduct, temp);
				getInventory().setStack(0, newProduct);
				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
			} else {
				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 0.8f);
			}
			ingotCrafting = false;
			plannedProductId = null;
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

			int[] offset = Positioning.getItemTextureOffset(getInventory().getStack(0));
			Vec2f offsetVec = new Vec2f(offset[0] / 16.0f, offset[1] / 16.0f);

			net.minecraft.util.math.Vec3d worldParticlePos = SmithingAnvilBlockEntity.Positioning.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);

			// Distinct particle for hit
			// Reduced spread and speed for smaller particles
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

			int[] offset = Positioning.getItemTextureOffset(itemStack);
			Vec2f offsetVec = new Vec2f(offset[0] / 16.0f, offset[1] / 16.0f);

			net.minecraft.util.math.Vec3d worldParticlePos = SmithingAnvilBlockEntity.Positioning.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);

			// Reduced spread and speed for smaller particles
			serverWorld.spawnParticles(ParticleTypes.END_ROD, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 1, 0.005, 0.005, 0.005, 0.01);
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
		if (stackForMarker.isEmpty() || markerAttempts >= TOTAL_MARKERS) {
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
					Vec2f marker = SmithingAnvilBlockEntity.Positioning.getRandomMarkerPosition(stackForMarker, getCachedState());
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
		List<Identifier> options = findAvailableSchematicProductsForPlayer(player);
		if (options.isEmpty()) {
			player.sendMessage(Text.literal("You have no schematics for this material."), true);
			return;
		}

		PacketByteBuf data = PacketByteBufs.create();
		data.writeBlockPos(getPos());
		data.writeInt(options.size());
		for (Identifier id : options) {
			data.writeIdentifier(id);
		}
		// Use the shared channel so the client can receive and open the UI
		ServerPlayNetworking.send((ServerPlayerEntity) player, ModMessages.OPEN_SCHEMATIC_SELECTION, data); // You may want to rename this channel
	}

	public void setPlannedProduct(Identifier productId) {
		this.plannedProductId = productId;
		markDirty();
		// Reset the mini-game so it starts fresh after selection
		resetMarkerProgress();
	}

	private List<Identifier> findAvailableSchematicProductsForPlayer(PlayerEntity player) {
		List<Identifier> result = new ArrayList<>();
		var inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isEmpty()) continue;
			deriveProductIdFromSchematic(s).ifPresent(id -> {
				if (!result.contains(id)) {
					result.add(id);
				}
			});
		}
		return result;
	}

	private java.util.Optional<Identifier> deriveProductIdFromSchematic(ItemStack schematicStack) {
		// Translation key ending in "-schematic" maps to product by stripping suffix.
		String key = schematicStack.getItem().getTranslationKey();
		if (key.endsWith("-schematic")) {
			String base = key.substring(0, key.length() - "-schematic".length());
			// Try to convert translationKey-like "item.forgero.axe_head" to Identifier "forgero:axe_head"
			int nsIdx = base.indexOf('.');
			if (nsIdx >= 0 && nsIdx < base.length() - 1) {
				String afterPrefix = base.substring(nsIdx + 1);
				int typeIdx = afterPrefix.indexOf('.');
				if (typeIdx >= 0 && typeIdx < afterPrefix.length() - 1) {
					String namespace = afterPrefix.substring(0, typeIdx);
					String path = afterPrefix.substring(typeIdx + 1);
					try {
						return java.util.Optional.of(new Identifier(namespace, path));
					} catch (Exception ignored) {
					}
				}
			}
		}
		return java.util.Optional.empty();
	}

	private ItemStack createProductFromPlanned(Identifier productId) {
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

	// ...existing Positioning class...

	// =================================
	// Positioning Utility Class
	// =================================

	public static class Positioning {
		private static final BoundingBoxUtil boundingBoxUtil = new BoundingBoxUtil();
		private static final Random random = new Random();

		/**
		 * Converts a world-space hit result into the item's local texture space (-0.5 to 0.5).
		 * This function needs to inverse the transformations applied in the renderer.
		 *
		 * @param hit               The BlockHitResult from the player's interaction.
		 * @param anvilState        The BlockState of the Smithing Anvil, providing its facing direction.
		 * @param itemTextureOffset The (dx, dz) texture offset for the item in block units (0-1 range, e.g., 2/16 = 0.125).
		 * @return Vec2f representing the hit position in the item's local texture space (-0.5 to 0.5 for X,Y).
		 */
		public static Vec2f worldHitToItemLocal(BlockHitResult hit, BlockState anvilState, Vec2f itemTextureOffset) {
			// Step 1: Convert world hit position to coordinates relative to the block's center (range -0.5..0.5)
			double localX_block_center = hit.getPos().x - hit.getBlockPos().getX() - 0.5;
			double localZ_block_center = hit.getPos().z - hit.getBlockPos().getZ() - 0.5;

			// Step 2: Inverse of anvil rotation
			Direction facing = anvilState.get(com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.FACING);
			float anvilAngleDegrees = 0.0f;
			switch (facing) {
				case EAST -> anvilAngleDegrees = -90.0f;
				case SOUTH -> anvilAngleDegrees = 180.0f;
				case WEST -> anvilAngleDegrees = 90.0f;
				case NORTH -> anvilAngleDegrees = 0.0f;
			}
			float invAnvilAngleRadians = (float) Math.toRadians(-anvilAngleDegrees);
			double cosInv = Math.cos(invAnvilAngleRadians);
			double sinInv = Math.sin(invAnvilAngleRadians);

			double xAfterAnvilRot = localX_block_center * cosInv - localZ_block_center * sinInv;
			double zAfterAnvilRot = localX_block_center * sinInv + localZ_block_center * cosInv;

			// Step 3: Inverse of item's 180-degree rotation (rotation by -180 is equivalent to negation)
			float xBeforeItemRot = (float) -xAfterAnvilRot;
			float zBeforeItemRot = (float) -zAfterAnvilRot;

			// Step 4: Inverse of texture offset (offset was applied before scaling in renderer)
			float xBeforeOffset = xBeforeItemRot - itemTextureOffset.x;
			float zBeforeOffset = zBeforeItemRot - itemTextureOffset.y;

			// Step 5: Inverse of scale
			float xLocal = xBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
			float zLocal = zBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

			// Return item-local coordinates in the same space as markerPositions (no extra negation)
			return new Vec2f(xLocal, zLocal);
		}

		/**
		 * Converts an item's local texture space coordinate (-0.5 to 0.5) to a world-space position for rendering particles.
		 * This function applies the transformations in the same order as the renderer, but for a single point.
		 *
		 * @param itemLocalPos      The position in the item's local texture space (-0.5 to 0.5 for X,Y).
		 * @param anvilBlockPos     The BlockPos of the Smithing Anvil.
		 * @param anvilState        The BlockState of the Smithing Anvil.
		 * @param itemTextureOffset The (dx, dz) texture offset for the item.
		 * @param baseY             The base Y-coordinate offset relative to the block's origin (0-1 range).
		 * @return Vec3d representing the world coordinates where the particle should spawn.
		 */
		public static Vec3d itemLocalToWorld(Vec2f itemLocalPos, BlockPos anvilBlockPos, BlockState anvilState, Vec2f itemTextureOffset, float baseY) {
			// These steps mirror the transformation chain in SmithingAnvilBlockEntityRenderer.render

			// Step 1: Apply texture centering offset (in unscaled item local space).
			float transformedX_preScale = itemLocalPos.x + itemTextureOffset.x;
			float transformedZ_preScale = itemLocalPos.y + itemTextureOffset.y; // itemLocalPos.y is equivalent to Z in world space

			// Step 2: Apply the RENDER_SCALE_FACTOR.
			float transformedX_scaled = transformedX_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
			float transformedZ_scaled = transformedZ_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

			// Step 3: Apply Item's 180-degree rotation.
			float transformedX_afterItemRot = -transformedX_scaled;
			float transformedZ_afterItemRot = -transformedZ_scaled;

			// Step 4: Apply Anvil's Rotation (to align with the block's orientation)
			// Use the same anvilAngleDegrees as the renderer.
			Direction facing = anvilState.get(com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.FACING);
			float anvilAngleDegrees = 0.0f;
			switch (facing) {
				case EAST -> anvilAngleDegrees = -90.0f;
				case SOUTH -> anvilAngleDegrees = 180.0f;
				case WEST -> anvilAngleDegrees = 90.0f;
				case NORTH -> anvilAngleDegrees = 0.0f;
			}
			float angleRadians = (float) Math.toRadians(anvilAngleDegrees);
			float cos = (float) Math.cos(angleRadians);
			float sin = (float) Math.sin(angleRadians);
			double rotatedX = transformedX_afterItemRot * cos - transformedZ_afterItemRot * sin;
			double rotatedZ = transformedX_afterItemRot * sin + transformedZ_afterItemRot * cos;

			// Step 5: Final world coordinates (translation to block center).
			double worldX = anvilBlockPos.getX() + 0.5 + rotatedX;
			double worldY = anvilBlockPos.getY() + baseY; // Use the provided baseY
			double worldZ = anvilBlockPos.getZ() + 0.5 + rotatedZ;

			return new net.minecraft.util.math.Vec3d(worldX, worldY, worldZ);
		}

		/**
		 * Checks if a normalized (x, z) in item-local space is inside the top face of the *unscaled* anvil's voxel shape.
		 * This is used for generating markers, ensuring they appear on the anvil's surface, not off it.
		 *
		 * @param itemLocalX     The X coordinate in item's local texture space (-0.5 to 0.5).
		 * @param itemLocalZ     The Z coordinate in item's local texture space (-0.5 to 0.5).
		 * @param anvilState     The BlockState of the Smithing Anvil.
		 * @param anvilItemStack The ItemStack currently on the anvil, used to get its texture offset.
		 * @return True if the point, when scaled to block space, is within the anvil's top layer.
		 */
		private static boolean isInsideAnvilTopLayer(float itemLocalX, float itemLocalZ, BlockState anvilState, ItemStack anvilItemStack) {
			Direction facing = anvilState.get(com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.FACING);
			VoxelShape shape = switch (facing) {
				case NORTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_NORTH;
				case SOUTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_SOUTH;
				case EAST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_EAST;
				case WEST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_WEST;
				default -> VoxelShapes.fullCube();
			};

			// To check against the anvil's VoxelShape (which is defined in unscaled 0-1 block coordinates),
			// we apply the *forward* transformations from itemLocal space to block space.

			// Step 1: Apply texture centering offset (in unscaled item local space).
			Vec2f itemTextureOffset = new Vec2f(Positioning.getItemTextureOffset(anvilItemStack)[0] / 16.0f, Positioning.getItemTextureOffset(anvilItemStack)[1] / 16.0f);

			float transformedX_preScale = itemLocalX + itemTextureOffset.x;
			float transformedZ_preScale = itemLocalZ + itemTextureOffset.y;

			// Step 2: Apply the RENDER_SCALE_FACTOR.
			float transformedX_scaled = transformedX_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
			float transformedZ_scaled = transformedZ_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

			// Step 3: Apply Item's 180-degree rotation.
			float transformedX_afterItemRot = -transformedX_scaled;
			float transformedZ_afterItemRot = -transformedZ_scaled;


			// Step 4: Apply Anvil's Rotation (to align with the block's orientation)
			// Use the same anvilAngleDegrees as the renderer.
			Direction anvilFacing = anvilState.get(com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.FACING);
			float anvilAngleDegrees = 0.0f;
			switch (anvilFacing) {
				case EAST -> anvilAngleDegrees = -90.0f;
				case SOUTH -> anvilAngleDegrees = 180.0f;
				case WEST -> anvilAngleDegrees = 90.0f;
				case NORTH -> anvilAngleDegrees = 0.0f;
			}
			float angleRadians = (float) Math.toRadians(anvilAngleDegrees);
			float cos = (float) Math.cos(angleRadians);
			float sin = (float) Math.sin(angleRadians);

			float finalX_block_center = transformedX_afterItemRot * cos - transformedZ_afterItemRot * sin;
			float finalZ_block_center = transformedX_afterItemRot * sin + transformedZ_afterItemRot * cos;


			// Step 5: Shift to 0-1 range for VoxelShape comparison.
			double testX = finalX_block_center + 0.5;
			double testZ = finalZ_block_center + 0.5;
			double testY = 1.0 - 1e-6; // Check slightly below the top surface of the anvil.

			for (net.minecraft.util.math.Box box : shape.getBoundingBoxes()) {
				if (box.contains(testX, testY, testZ)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Gets a random marker position within the item's texture, in local coordinates (-0.5 to 0.5).
		 *
		 * @param stack      The ItemStack representing the item on the anvil.
		 * @param anvilState The BlockState of the Smithing Anvil.
		 * @return Vec2f representing a random valid marker position in the item's local texture space, or Vec2f.ZERO if no valid position can be found.
		 */
		public static Vec2f getRandomMarkerPosition(ItemStack stack, BlockState anvilState) {
			try {
				MinecraftClient client = MinecraftClient.getInstance();
				BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
				if (image == null) return Vec2f.ZERO;

				List<java.awt.Point> validPixels = boundingBoxUtil.collectValidPixels(image);
				if (validPixels.isEmpty()) return Vec2f.ZERO;

				for (int attempt = 0; attempt < 32; attempt++) {
					java.awt.Point p = validPixels.get(random.nextInt(validPixels.size()));
					// Convert pixel coordinates (0-15) to item local coordinates (-0.5 to 0.5)
					float markerX_local = (p.x + 0.5f) / 16.0f - 0.5f;
					float markerZ_local = (p.y + 0.5f) / 16.0f - 0.5f;

					if (isInsideAnvilTopLayer(markerX_local, markerZ_local, anvilState, stack)) { // Pass the stack here
						return new Vec2f(markerX_local, markerZ_local);
					}
				}
				LOGGER.warn("Could not find a valid marker position for stack {} after 32 attempts.", stack.getName().getString());
				return Vec2f.ZERO;
			} catch (Exception e) {
				LOGGER.error("Error generating random marker position for stack {}: {}", stack.getName().getString(), e.getMessage());
				return Vec2f.ZERO;
			}
		}

		/**
		 * Retrieves the texture offset for a given ItemStack from its cached model.
		 * Used to correctly center the item's visible part.
		 *
		 * @param stack The ItemStack to get the offset for.
		 * @return An int array [dx, dz] in pixels (0-15 range), or [0,0] if not found.
		 */
		public static int[] getItemTextureOffset(ItemStack stack) {
			if (stack.isEmpty()) {
				return new int[]{0, 0};
			}
			try {
				MinecraftClient client = MinecraftClient.getInstance();
				BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
				if (image != null) {
					return BoundingBoxUtil.getItemTextureOffsetFromImage(image);
				}
			} catch (Exception e) {
				// Fallthrough
			}
			return new int[]{0, 0};
		}
	}

	// Client-only setter used by S2C sync to reflect ingot crafting state without resetting markers/minigame.
	public void clientSyncIngotState(boolean ingotCrafting, @Nullable Identifier plannedProductId) {
		this.ingotCrafting = ingotCrafting;
		this.plannedProductId = plannedProductId;
	}

}
