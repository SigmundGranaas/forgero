package com.sigmundgranaas.forgero.smithing.block.entity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.state.Typed;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Getter
public class SmithingAnvilBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroSmithingAnvil");
	private static final @NotNull String INVENTORY_NBT_KEY = "inventory";

	private @NotNull SimpleInventory inventory = new SimpleInventory(1);

	private int hammerHits = 0;
	private List<Vec2f> markerPositions = new ArrayList<>();
	private List<Boolean> markerHits = new ArrayList<>();
	private int markerAttempts = 0;
	private int markerHitsCount = 0;

	// --- Marker timing fields ---
	private int markerTicks = 0;
	private int markerTimeout = 0;
	private int markerCooldown = 0;
	private int markerSpawnDelay = 3;
	private int nextMarkerDelay = 3;
	private static final int FIRST_MARKER_DELAY_TICKS = 2;
	private static final int SUBSEQUENT_MARKER_DELAY_TICKS = 2;
	private static final int MIN_COOLDOWN_TICKS = 40;
	private static final int MAX_COOLDOWN_TICKS = 100;
	private static final int MARKER_LIFETIME_TICKS = 30;
	private final Random random = new Random();

	private static final int ANVIL_INVENTORY_COOL_PER_TICK = 1;
	private static final int ANVIL_INVENTORY_COOL_TICK_INTERVAL = 20;

	private int anvilInventoryCoolTickCounter = 0;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";

	private static final int TOTAL_MARKERS = 10;
	private static final int FAST_MARKERS = 5; // Number of fast/red markers

	private final List<Integer> fastMarkerIndices = new ArrayList<>();

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
	}

	@Override
	public void markDirty() {
		if (world == null) {
			return;
		}

		super.markDirty();

		if (world.isClient()) {
			return;
		}

		// Sync to clients
		@NotNull PacketByteBuf data = PacketByteBufs.create();
		@NotNull SimpleInventory inventory = getInventory();
		int inventorySize = getInventory().size();
		data.writeInt(inventorySize);
		for (int i = 0; i < inventorySize; i++) {
			data.writeItemStack(inventory.getStack(i));
		}
		data.writeBlockPos(getPos());

		// --- Sync marker positions and hits ---
		data.writeInt(markerPositions.size());
		for (int i = 0; i < markerPositions.size(); i++) {
			Vec2f pos = markerPositions.get(i);
			data.writeFloat(pos.x);
			data.writeFloat(pos.y);
			boolean hit = markerHits.size() > i && markerHits.get(i);
			data.writeBoolean(hit);
		}

		// --- Sync fast marker indices ---
		data.writeInt(fastMarkerIndices.size());
		for (int i = 0; i < fastMarkerIndices.size(); i++) {
			data.writeInt(fastMarkerIndices.get(i));
		}

		// --- Sync markerAttempts and markerHitsCount ---
		data.writeInt(markerAttempts);
		data.writeInt(markerHitsCount);

		for (@NotNull ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}
	}

	@Override
	public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.put(INVENTORY_NBT_KEY, this.getInventory().toNbtList());
		nbt.putInt("hammerHits", hammerHits);
		// Store marker positions
		for (int i = 0; i < markerPositions.size(); i++) {
			Vec2f pos = markerPositions.get(i);
			NbtCompound markerNbt = new NbtCompound();
			markerNbt.putFloat("x", pos.x);
			markerNbt.putFloat("y", pos.y);
			markerNbt.putBoolean("hit", markerHits.size() > i && markerHits.get(i));
			nbt.put("marker_" + i, markerNbt);
		}
		// Save fast marker indices
		nbt.putIntArray("fastMarkerIndices", fastMarkerIndices);
		ItemStack stack = inventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
			itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		}
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);
		this.getInventory().readNbtList(nbt.getList(INVENTORY_NBT_KEY, NbtElement.LIST_TYPE));
		this.hammerHits = nbt.getInt("hammerHits");
		// Load marker positions
		markerPositions.clear();
		markerHits.clear();
		for (int i = 0; i < TOTAL_MARKERS; i++) {
			if (nbt.contains("marker_" + i)) {
				NbtCompound markerNbt = nbt.getCompound("marker_" + i);
				markerPositions.add(new Vec2f(markerNbt.getFloat("x"), markerNbt.getFloat("y")));
				markerHits.add(markerNbt.getBoolean("hit"));
			}
		}
		// Restore fast marker indices
		fastMarkerIndices.clear();
		if (nbt.contains("fastMarkerIndices")) {
			for (int idx : nbt.getIntArray("fastMarkerIndices")) {
				fastMarkerIndices.add(idx);
			}
		}
		// Restore progress from item NBT if present
		ItemStack stack = inventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
			this.markerAttempts = itemNbt.getInt(ATTEMPTS_NBT_KEY);
		} else {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
		}
	}

	// Helper: check if a normalized (x, z) is inside the top face of the anvil's voxel shape
	private boolean isInsideAnvilTopLayer(float x, float z) {
		// Get the anvil's facing direction
		Direction facing = getCachedState().getOrEmpty(com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.FACING)
				.orElse(Direction.NORTH);
		VoxelShape shape = switch (facing) {
			case NORTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_NORTH;
			case SOUTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_SOUTH;
			case EAST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_EAST;
			case WEST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_WEST;
			default -> VoxelShapes.fullCube();
		};
		// The top layer is at y = 1.0 (normalized)
		double testY = 1.0 - 1e-6; // Slightly below 1.0 to avoid floating point issues
		// Test if the point (x, testY, z) is inside the shape
		for (net.minecraft.util.math.Box box : shape.getBoundingBoxes()) {
			if (box.contains(x, testY, z)) {
				return true;
			}
		}
		return false;
	}

	// Helper: get a random marker position within the bounding box, centered as in the renderer, and inside the anvil top layer
	private Vec2f getRandomMarkerPosition(ItemStack stack) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);
			var quads = model.getQuads(null, null, client.world.getRandom());
			java.util.Set<Identifier> loggedTextures = new java.util.HashSet<>();
			Sprite textureSprite = null;
			for (var quad : quads) {
				Sprite quadSprite = quad.getSprite();
				Identifier quadSpriteId = quadSprite.getContents().getId();
				Identifier quadResourceId = new Identifier(quadSpriteId.getNamespace(), "textures/" + quadSpriteId.getPath() + ".png");
				if (loggedTextures.add(quadResourceId)) {
					LOGGER.info("[SmithingAnvil] Quad PNG resource: {}", quadResourceId);
				}
				if (textureSprite == null) {
					textureSprite = quadSprite;
				}
			}
			if (textureSprite == null) {
				// Fallback to particle sprite
				textureSprite = model.getParticleSprite();
			}
			Identifier spriteId = textureSprite.getContents().getId();
			Identifier resourceId = new Identifier(spriteId.getNamespace(), "textures/" + spriteId.getPath() + ".png");
			LOGGER.info("[SmithingAnvil] Using PNG resource for marker: {}", resourceId);
			BufferedImage image;
			try (java.io.InputStream stream = client.getResourceManager().getResource(resourceId).get().getInputStream()) {
				image = ImageIO.read(stream);
			}
			BoundingBoxUtil util = new BoundingBoxUtil();
			BoundingBoxUtil.BoundingBox box = util.calculateBoundingBox(image);
			int validPixelCount = util.collectValidPixels(image).size();
			LOGGER.info("[SmithingAnvil] BoundingBox for {}: minX={}, minY={}, maxX={}, maxY={}, validPixels={}", resourceId, box.minX(), box.minY(), box.maxX(), box.maxY(), validPixelCount);
			if (validPixelCount > 0) {
				java.awt.Point offset = box.getCenteringOffset16x16();
				LOGGER.info("[SmithingAnvil] Centering offset for {}: x={}, y={}", resourceId, offset.x, offset.y);

				// Calculate the same offset as the renderer
				int[] textureOffset = BoundingBoxUtil.getItemTextureOffsetFromImage(image);
				float dx = textureOffset[0] / 16.0f;
				float dz = textureOffset[1] / 16.0f;

				// Try up to 32 times to find a marker inside the anvil top layer
				for (int attempt = 0; attempt < 32; attempt++) {
					java.util.List<java.awt.Point> validPixels = util.collectValidPixels(image);
					java.awt.Point p = validPixels.get(random.nextInt(validPixels.size()));
					int centeredX = p.x + offset.x;
					int centeredY = p.y + offset.y;
					centeredX = Math.max(0, Math.min(15, centeredX));
					centeredY = Math.max(0, Math.min(15, centeredY));
					float markerX = (centeredX + 0.5f) / 16.0f + dx;
					float markerY = (centeredY + 0.5f) / 16.0f + dz;
					// markerX = x, markerY = z (since marker is on top face)
					if (isInsideAnvilTopLayer(markerX, markerY)) {
						LOGGER.info("[SmithingAnvil] Marker pixel: ({}, {}), Centered: ({}, {}), Normalized: ({}, {}) [INSIDE]", p.x, p.y, centeredX, centeredY, markerX, markerY);
						return new Vec2f(markerX, markerY);
					} else {
						LOGGER.info("[SmithingAnvil] Marker pixel: ({}, {}), Centered: ({}, {}), Normalized: ({}, {}) [OUTSIDE]", p.x, p.y, centeredX, centeredY, markerX, markerY);
					}
				}
				// If none found, fallback to center with offset
				return new Vec2f(0.5f + dx, 0.5f + dz);
			} else {
				LOGGER.warn("[SmithingAnvil] No valid pixels found in bounding box for {}", resourceId);
				return new Vec2f(0.5f, 0.5f);
			}
		} catch (Exception e) {
			LOGGER.error("[SmithingAnvil] Error generating marker position: ", e);
			// fallback: center
			return new Vec2f(0.5f, 0.5f);
		}
	}

	public void generateSingleMarker() {
		ItemStack stack = inventory.getStack(0);
		int temp = TemperatureUtils.getTemperature(stack);
		LOGGER.info("[SmithingAnvil] generateSingleMarker called. Stack: {}, Temp: {}", stack, temp);
		if (stack.isEmpty() || temp < 400 || temp > 600) {
			LOGGER.info("[SmithingAnvil] No marker generated: stack empty or temp out of range.");
			markerPositions.clear();
			markerHits.clear();
			markDirty();
			return;
		}
		markerPositions.clear();
		markerHits.clear();

		// --- Use random marker position within bounding box ---
		Vec2f marker = getRandomMarkerPosition(stack);
		LOGGER.info("[SmithingAnvil] Generated marker at: {}", marker);
		markerPositions.add(marker);
		markerHits.add(false);
		markDirty();
	}

	// Only call this on the server! Generates new fast marker indices.
	public void resetMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();
		markerAttempts = 0;
		markerHitsCount = 0;
		markerSpawnDelay = FIRST_MARKER_DELAY_TICKS;
		nextMarkerDelay = FIRST_MARKER_DELAY_TICKS;
		fastMarkerIndices.clear();
		// Pick 3 unique random indices for fast markers (values 0-9, corresponding to markerAttempts)
		while (fastMarkerIndices.size() < FAST_MARKERS) {
			int idx = random.nextInt(TOTAL_MARKERS);
			if (!fastMarkerIndices.contains(idx)) {
				fastMarkerIndices.add(idx);
			}
		}
		markDirty();
	}

	// Use this on the client to clear state, but NOT generate new fast markers
	public void clearMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();
		markerAttempts = 0;
		markerHitsCount = 0;
		markerSpawnDelay = FIRST_MARKER_DELAY_TICKS;
		nextMarkerDelay = FIRST_MARKER_DELAY_TICKS;
	}

	public boolean hasActiveMarker() {
		return !markerPositions.isEmpty() && markerAttempts < TOTAL_MARKERS;
	}

	public int getMarkerAttempts() {
		return markerAttempts;
	}

	public int getMarkerHitsCount() {
		return markerHitsCount;
	}

	public void processMarkerAttempt(boolean hit) {
		if (markerAttempts >= TOTAL_MARKERS) return;
		// Fast markers should always count as an attempt, even if missed
		boolean isFast = fastMarkerIndices.contains(markerAttempts);
		markerAttempts++;
		ItemStack stack = inventory.getStack(0);
		int temp = TemperatureUtils.getTemperature(stack);
		int depletion = hit ? 40 : 10; // 40 for hit, 10 for miss
		if (hit) {
			markerHitsCount++;
		}
		// --- Deplete temperature on every attempt ---
		int newTemp = Math.max(TemperatureUtils.MIN_TEMPERATURE, temp - depletion);
		TemperatureUtils.setTemperature(stack, newTemp);
		markDirty();
		markerPositions.clear();
		markerHits.clear();
		// Always set up for next marker if not done, regardless of hit or miss, including fast markers
		if (markerAttempts < TOTAL_MARKERS) {
			nextMarkerDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
			markerSpawnDelay = nextMarkerDelay;
			markerCooldown = 0;
		} else {
			markDirty();
		}
	}

	// Mark a marker as hit and sync
	public void setMarkerHit(int index) {
		if (index >= 0 && index < markerHits.size()) {
			markerHits.set(index, true);
			// Play anvil use sound when marker is hit
			if (world != null && !world.isClient) {
				world.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
				// Spawn sparks particles when marker is hit
				if (world instanceof ServerWorld serverWorld) {
					Vec2f marker = markerPositions.size() > index ? markerPositions.get(index) : new Vec2f(0.5f, 0.5f);
					double px = getPos().getX() + marker.x;
					double py = getPos().getY() + 1.05;
					double pz = getPos().getZ() + marker.y;
					for (int i = 0; i < 8; i++) {
						serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT, px, py, pz, 1, 0.1, 0.05, 0.1, 0.15);
					}
				}
			}
			// Remove marker immediately after hit
			markerPositions.clear();
			markerHits.clear();
			markDirty();
		}
	}

	// Play anvil hit sound when the player misses the marker
	public void playMissSound() {
		if (world != null && !world.isClient) {
			world.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}
	}

	// Reset all markers and sync
	public void resetMarkers() {
		resetMarkerProgress();
	}

	// Client-side tick for spawning firework particles at unhit marker positions
	public void clientTick() {
		if (this.world == null || !this.world.isClient) return;
		if (inventory.getStack(0).isEmpty()) return;
		if (markerPositions.size() == 1 && markerAttempts < TOTAL_MARKERS) {
			Vec2f marker = markerPositions.get(0);
			double worldX = this.getPos().getX() + marker.x;
			double worldY = this.getPos().getY() + 1.05;
			double worldZ = this.getPos().getZ() + marker.y;
			boolean isFast = fastMarkerIndices.contains(markerAttempts);
			for (int i = 0; i < 2; i++) {
				if (isFast) {
					this.world.addParticle(
							new net.minecraft.particle.DustParticleEffect(
									new Vector3f(1.0f, 0.0f, 0.0f),
									0.27f
							),
							worldX, worldY, worldZ,
							0.02, 0.02, 0.02
					);
				} else {
					this.world.addParticle(
						new net.minecraft.particle.DustParticleEffect(
							new Vector3f(1.0f, 1.0f, 0.0f), // Yellow color
							0.27f
						),
						worldX, worldY, worldZ,
						0.02, 0.02, 0.02
					);
				}
			}
		}
	}

	public void tick() {
		if (this.world != null && this.world.isClient) {
			this.clientTick();
			return;
		}

		// --- Inventory cooling for item in anvil slot (now matches world tick rate) ---
		anvilInventoryCoolTickCounter++;
		if (anvilInventoryCoolTickCounter >= ANVIL_INVENTORY_COOL_TICK_INTERVAL) {
			anvilInventoryCoolTickCounter = 0;
			ItemStack stack = inventory.getStack(0);
			if (!stack.isEmpty()) {
				int temp = TemperatureUtils.getTemperature(stack);
				if (temp > 20) {
					temp = Math.max(20, temp - ANVIL_INVENTORY_COOL_PER_TICK);
					TemperatureUtils.setTemperature(stack, temp);
					markDirty();
				}
			}
			// --- Marker spawn logic with delay ---
			int temp = TemperatureUtils.getTemperature(stack);
			boolean valid = !stack.isEmpty() && ToolPartTypeUtils.isToolPartType(
					StateService.INSTANCE.convert(stack)
							.filter(s -> s instanceof Typed)
							.map(s -> ((Typed) s).type())
							.orElse(null)
			);
			int maxTemp = TemperatureUtils.getMaxTemp(stack);
			boolean inTemp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isOrangeGroupStage(temp, maxTemp);

			// --- Prevent marker spawning if tool already has a condition ---
			boolean hasCondition = false;
			if (!stack.isEmpty()) {
				var stateOpt = StateService.INSTANCE.convert(stack);
				if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional) {
					hasCondition = !conditional.localConditions().isEmpty();
				}
			}

			if (valid && inTemp && !hasCondition) {
				// --- If a marker timed out (missed), automatically advance to next marker ---
				if (markerPositions.isEmpty() && markerCooldown > 0 && markerAttempts < TOTAL_MARKERS) {
					// Marker was missed, so prepare to spawn the next marker after the delay
					// Advance the attempt if the last marker was a fast marker
					if (fastMarkerIndices.contains(markerAttempts)) {
						// Fast marker missed: count as an attempt and advance
						markerAttempts++;
						nextMarkerDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
						markerSpawnDelay = nextMarkerDelay;
						markerCooldown = 0;
						markDirty();
					} else {
						// Normal marker missed: old behavior
						markerCooldown--;
						if (markerCooldown <= 0) {
							nextMarkerDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
							markerSpawnDelay = nextMarkerDelay;
						}
					}
				}
				if (markerPositions.isEmpty() && markerCooldown <= 0 && markerAttempts < TOTAL_MARKERS) {
					if (markerSpawnDelay > 0) {
						markerSpawnDelay--;
					}
					if (markerSpawnDelay == 0) {
						markerPositions.clear();
						markerHits.clear();
						// --- Use random marker position within bounding box ---
						Vec2f marker = getRandomMarkerPosition(stack);
						LOGGER.info("[SmithingAnvil] (tick) Generated marker at: {}", marker);
						markerPositions.add(marker);
						markerHits.add(false);
						if (fastMarkerIndices.contains(markerAttempts)) {
							markerTimeout = 1;
						} else {
							markerTimeout = MARKER_LIFETIME_TICKS;
						}
						markDirty();
						nextMarkerDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
						markerSpawnDelay = -1;
					}
				} else if (!markerPositions.isEmpty()) {
					// Marker is active, count down its lifetime
					markerTimeout--;
					if (markerTimeout <= 0) {
						markerPositions.clear();
						markerHits.clear();
						// Instead of setting a random cooldown, always set markerCooldown to 1 to trigger auto-advance
						markerCooldown = 1;
						markDirty();
					}
				} else if (markerCooldown > 0) {
					markerCooldown--;
				}
			} else {
				// Not in valid temp range or not a tool part or already has condition: clear markers, timers, and delay
				if (!markerPositions.isEmpty() || markerCooldown > 0 || markerSpawnDelay > 0) {
					markerPositions.clear();
					markerHits.clear();
					markerCooldown = 0;
					markerTimeout = 0;
					markerSpawnDelay = FIRST_MARKER_DELAY_TICKS;
					nextMarkerDelay = FIRST_MARKER_DELAY_TICKS;
					markDirty();
				}
			}
		}
	}

	// Persist marker progress to the item NBT
	public void saveProgressToItem() {
		ItemStack stack = inventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
			itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
			// Save fastMarkerIndices to NBT
			itemNbt.putIntArray("fastMarkerIndices", fastMarkerIndices);
		}
	}

	// Restore marker progress from the item NBT
	public void loadProgressFromItem() {
		ItemStack stack = inventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
			this.markerAttempts = itemNbt.getInt(ATTEMPTS_NBT_KEY);
			// Restore fastMarkerIndices from NBT only if present and non-empty
			if (itemNbt.contains("fastMarkerIndices")) {
				int[] arr = itemNbt.getIntArray("fastMarkerIndices");
				if (arr.length > 0) {
					fastMarkerIndices.clear();
					for (int idx : arr) {
						fastMarkerIndices.add(idx);
					}
				}
			}
		} else {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			// Do not clear fastMarkerIndices here; let sync handle it
		}
	}

	// Setter for markerAttempts (needed for client sync)
	public void setMarkerAttempts(int markerAttempts) {
		this.markerAttempts = markerAttempts;
	}

	// Setter for markerHitsCount (needed for client sync)
	public void setMarkerHitsCount(int markerHitsCount) {
		this.markerHitsCount = markerHitsCount;
	}
}
