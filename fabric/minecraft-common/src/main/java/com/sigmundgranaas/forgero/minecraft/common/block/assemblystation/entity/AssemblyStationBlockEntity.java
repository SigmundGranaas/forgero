package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import net.minecraft.util.math.BlockPos;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry.ASSEMBLY_STATION_BLOCK_ENTITY;
import static net.minecraft.block.ChestBlock.getInventory;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	private static final @NotNull String DISASSEMBLY_INVENTORY_NBT_KEY = "DisassemblyInventory";
	private static final @NotNull String RESULT_INVENTORY_NBT_KEY = "ResultInventory";

	private final @NotNull SimpleInventory disassemblyInventory = new SimpleInventory(DISASSEMBLY_INVENTORY_SIZE);
	private final @NotNull SimpleInventory resultInventory = new SimpleInventory(RESULT_INVENTORY_SIZE);

	public AssemblyStationBlockEntity(@NotNull BlockPos blockPosition, @NotNull BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPosition, blockState);

		this.disassemblyInventory.addListener(inventory -> this.markDirtyAndUpdateListeners());
		this.resultInventory.addListener(inventory -> this.markDirtyAndUpdateListeners());
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(ASSEMBLY_STATION_TRANSLATION_KEY);
	}

	@Override
	public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new AssemblyStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(this.world, this.pos),
				this.getDisassemblyInventory(), this.getResultInventory()
		);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		nbt.put(DISASSEMBLY_INVENTORY_NBT_KEY, this.getDisassemblyInventory().toNbtList());
		nbt.put(RESULT_INVENTORY_NBT_KEY, this.getResultInventory().toNbtList());
		super.writeNbt(nbt);
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		this.getDisassemblyInventory().readNbtList(nbt.getList(DISASSEMBLY_INVENTORY_NBT_KEY, NbtElement.LIST_TYPE));
		this.getResultInventory().readNbtList(nbt.getList(RESULT_INVENTORY_NBT_KEY, NbtElement.LIST_TYPE));
		super.readNbt(nbt);
		this.markDirtyAndUpdateListeners();
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return super.createNbt();
	}

	@Override
	public @NotNull Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	public @NotNull SimpleInventory getDisassemblyInventory() {
		return this.disassemblyInventory;
	}

	public @NotNull SimpleInventory getResultInventory() {
		return this.resultInventory;
	}

	private void markDirtyAndUpdateListeners() {
		if (this.world == null) {
			return;
		}

		this.markDirty();
		this.world.updateListeners(this.pos, this.getCachedState(), this.world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}

	public ItemStack getRenderStack(){
		return this.disassemblyInventory.getStack(0);
	}

	@Override
	public void markDirty() {
		world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		super.markDirty();
	}


}
