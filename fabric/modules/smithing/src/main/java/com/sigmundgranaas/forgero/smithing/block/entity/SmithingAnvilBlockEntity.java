package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.networking.ModMessages;

import lombok.Getter;

import net.minecraft.block.Block;
import net.minecraft.inventory.SimpleInventory;

import net.minecraft.nbt.NbtElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Getter
public class SmithingAnvilBlockEntity extends BlockEntity {
	private static final @NotNull String INVENTORY_NBT_KEY = "inventory";

	private @NotNull SimpleInventory inventory = new SimpleInventory(1);

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
	}

	@Override
	public void markDirty() {
		if (world == null) {
			return;
		}
		if (world.isClient()) {
			super.markDirty();
			return;
		}

		@NotNull PacketByteBuf data = PacketByteBufs.create();
		@NotNull SimpleInventory inventory = getInventory();
		int inventorySize = getInventory().size();
		data.writeInt(inventorySize);
		for (int i = 0; i < inventorySize; i++) {
			data.writeItemStack(inventory.getStack(i));
		}
		data.writeBlockPos(getPos());

		for (@NotNull ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}

		super.markDirty();
	}

	@Override
	public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		nbt.put(INVENTORY_NBT_KEY, this.getInventory().toNbtList());
		super.writeNbt(nbt);
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		this.getInventory().readNbtList(nbt.getList(INVENTORY_NBT_KEY, NbtElement.LIST_TYPE));
		super.readNbt(nbt);
		this.markDirtyAndUpdateListeners();
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return super.createNbt();
	}

	public @NotNull ItemStack getRenderStack() {
		return this.getInventory().getStack(0);
	}

	public void setInventory(@NotNull SimpleInventory inventory) {
		this.inventory = inventory;
	}

	private void markDirtyAndUpdateListeners() {
		if (this.world == null) {
			return;
		}

		this.markDirty();
		this.world.updateListeners(this.pos, this.getCachedState(), this.world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}
}


