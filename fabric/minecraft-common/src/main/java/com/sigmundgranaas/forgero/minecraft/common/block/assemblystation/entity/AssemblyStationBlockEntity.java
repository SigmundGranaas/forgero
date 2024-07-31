package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry.ASSEMBLY_STATION_BLOCK_ENTITY;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	private final @NotNull SimpleInventory disassemblyInventory = new SimpleInventory(DISASSEMBLY_INVENTORY_SIZE);
	private final @NotNull SimpleInventory resultInventory = new SimpleInventory(RESULT_INVENTORY_SIZE);

	public AssemblyStationBlockEntity(@NotNull BlockPos blockPosition, @NotNull BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPosition, blockState);

		this.disassemblyInventory.addListener(this::onInventoryChanged);
		this.resultInventory.addListener(this::onInventoryChanged);
	}

	private void onInventoryChanged(@NotNull Inventory inventory) {
		if (this.world == null) {
			return;
		}

		this.markDirty();
		this.world.updateListeners(this.pos, this.getCachedState(), this.world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(ASSEMBLY_STATION_TRANSLATION_KEY);
	}

	@Override
	public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new AssemblyStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(this.world, this.pos),
				getDisassemblyInventory(), getResultInventory()
		);
	}

	// TODO: Ser/de
//	@Override
//	public void readNbt(NbtCompound nbt) {
//		resultInventory.clear();
//		disassemblyInventory.clear();
//		super.readNbt(nbt);
//		Inventories.readNbt(nbt, resultInventory.stacks);
//
//		if (nbt.contains("DisassemblyItems")) {
//			Inventories.readNbt(nbt.getCompound("DisassemblyItems"), disassemblyInventory.stacks);
//		}
//
//		updateListeners();
//	}
//
//	@Override
//	public void writeNbt(NbtCompound nbt) {
//		Inventories.writeNbt(nbt, resultInventory.stacks);
//
//		NbtCompound disassemblyNbt = new NbtCompound();
//		Inventories.writeNbt(disassemblyNbt, disassemblyInventory.stacks);
//		nbt.put("DisassemblyItems", disassemblyNbt);
//
//		super.writeNbt(nbt);
//	}

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
}
