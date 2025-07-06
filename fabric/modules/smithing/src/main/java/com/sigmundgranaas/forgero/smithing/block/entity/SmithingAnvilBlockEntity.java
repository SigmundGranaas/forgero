package com.sigmundgranaas.forgero.smithing.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.Typed;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Getter
public class SmithingAnvilBlockEntity extends BlockEntity {
	private static final @NotNull String INVENTORY_NBT_KEY = "inventory";

	private @NotNull SimpleInventory inventory = new SimpleInventory(1);
	private int hammerHits = 0;
	private List<Vec2f> markerPositions = new ArrayList<>();
	private List<Boolean> markerHits = new ArrayList<>();
	private int markerAttempts = 0;
	private int markerHitsCount = 0;

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
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);
		this.getInventory().readNbtList(nbt.getList(INVENTORY_NBT_KEY, NbtElement.LIST_TYPE));
		this.hammerHits = nbt.getInt("hammerHits");
		// Load marker positions
		markerPositions.clear();
		markerHits.clear();
		for (int i = 0; i < 3; i++) {
			if (nbt.contains("marker_" + i)) {
				NbtCompound markerNbt = nbt.getCompound("marker_" + i);
				markerPositions.add(new Vec2f(markerNbt.getFloat("x"), markerNbt.getFloat("y")));
				markerHits.add(markerNbt.getBoolean("hit"));
			}
		}
	}

	public void generateRandomMarkers() {
		System.out.println("[SmithingAnvilBlockEntity] generateRandomMarkers called");
		ItemStack stack = inventory.getStack(0);
		boolean isToolPart = false;
		if (!stack.isEmpty()) {
			System.out.println("[SmithingAnvilBlockEntity] Stack in slot: " + stack);
			var stateOpt = StateService.INSTANCE.convert(stack);
			System.out.println("[SmithingAnvilBlockEntity] StateService conversion present: " + stateOpt.isPresent());
			if (stateOpt.isPresent()) {
				System.out.println("[SmithingAnvilBlockEntity] StateService state: " + stateOpt.get());
				if (stateOpt.get() instanceof Typed) {
					Typed typed = (Typed) stateOpt.get();
					Type type = typed.type();
					System.out.println("[SmithingAnvilBlockEntity] Type: " + type.typeName());
					isToolPart = ToolPartTypeUtils.isToolPartHeadOrToolPart(type);
					System.out.println("[SmithingAnvilBlockEntity] isToolPartHeadOrToolPart: " + isToolPart);
				} else {
					System.out.println("[SmithingAnvilBlockEntity] State is not Typed, it is: " + stateOpt.get().getClass().getName());
				}
			}
		}
		if (stack.isEmpty() || !isToolPart) {
			System.out.println("[SmithingAnvilBlockEntity] No valid toolpart, clearing markers");
			markerPositions.clear();
			markerHits.clear();
			markDirty();
			return;
		}
		markerPositions.clear();
		markerHits.clear();
		for (int i = 0; i < 3; i++) {
			float x = 0.35f + (float) Math.random() * 0.3f;
			float y = 0.35f + (float) Math.random() * 0.3f;
			markerPositions.add(new Vec2f(x, y));
			markerHits.add(false);
			System.out.println("[SmithingAnvilBlockEntity] Generated marker " + i + ": (" + x + ", " + y + ")");
		}
		System.out.println("[SmithingAnvilBlockEntity] markerPositions size after generation: " + markerPositions.size());
		markDirty();
	}

	public void generateSingleMarker() {
		markerPositions.clear();
		markerHits.clear();
		float x = 0.35f + (float) Math.random() * 0.3f;
		float y = 0.35f + (float) Math.random() * 0.3f;
		markerPositions.add(new Vec2f(x, y));
		markerHits.add(false);
		markDirty();
	}

	public void resetMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();
		markerAttempts = 0;
		markerHitsCount = 0;
		markDirty();
	}

	public boolean hasActiveMarker() {
		return !markerPositions.isEmpty() && markerAttempts < 3;
	}

	public int getMarkerAttempts() {
		return markerAttempts;
	}

	public int getMarkerHitsCount() {
		return markerHitsCount;
	}

	public void processMarkerAttempt(boolean hit) {
		if (markerAttempts >= 3) return;
		markerAttempts++;
		if (hit) markerHitsCount++;
		markerPositions.clear();
		markerHits.clear();
		if (markerAttempts < 3) {
			generateSingleMarker();
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
			markDirty();
		}
	}

	// Play anvil hit sound when the player misses the marker
	public void playMissSound() {
		if (world != null && !world.isClient) {
			world.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
		// Removed tick interval check to spawn particle every tick for longer effect
		if (markerPositions.size() == 1 && markerAttempts < 3) {
			Vec2f marker = markerPositions.get(0);
			double worldX = this.getPos().getX() + marker.x;
			double worldY = this.getPos().getY() + 1.05;
			double worldZ = this.getPos().getZ() + marker.y;
			for (int i = 0; i < 2; i++) {
				this.world.addParticle(
					new net.minecraft.particle.DustParticleEffect(
						new Vector3f(1.0f, 0.5f, 0.0f), 0.2f),
					worldX, worldY, worldZ,
					0.0, 0.02, 0.0
				);
			}
		}
	}


	public void tick() {
		if (this.world != null && this.world.isClient) {
			this.clientTick();
		}
	}
}
