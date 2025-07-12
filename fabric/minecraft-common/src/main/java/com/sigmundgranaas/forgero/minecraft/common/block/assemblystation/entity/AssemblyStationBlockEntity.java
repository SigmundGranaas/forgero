package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry.ASSEMBLY_STATION_BLOCK_ENTITY;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	private static final @NotNull String DISASSEMBLY_INVENTORY_NBT_KEY = "DisassemblyInventory";
	private static final @NotNull String RESULT_INVENTORY_NBT_KEY = "ResultInventory";
	private static final @NotNull String INPUT_CONSUMED_NBT_KEY = "InputConsumed";
	private static final @NotNull String EXPECTED_RESULT_COUNT_NBT_KEY = "ExpectedResultCount";

	private final @NotNull SimpleInventory disassemblyInventory = new SimpleInventory(DISASSEMBLY_INVENTORY_SIZE);
	private final @NotNull SimpleInventory resultInventory = new SimpleInventory(RESULT_INVENTORY_SIZE);

	// Track if the input item has been consumed
	private boolean inputItemConsumed = false;

	// Track the expected number of result items
	private int expectedResultCount = 0;

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
		// We need to pass the current state to ensure consistency
		return new AssemblyStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(this.world, this.pos),
				this.getDisassemblyInventory(), this.getResultInventory(), this.inputItemConsumed, this.expectedResultCount
		);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		nbt.put(DISASSEMBLY_INVENTORY_NBT_KEY, this.getDisassemblyInventory().toNbtList());
		nbt.put(RESULT_INVENTORY_NBT_KEY, this.getResultInventory().toNbtList());
		nbt.putBoolean(INPUT_CONSUMED_NBT_KEY, this.inputItemConsumed);
		nbt.putInt(EXPECTED_RESULT_COUNT_NBT_KEY, this.expectedResultCount);
		super.writeNbt(nbt);
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);

		this.getDisassemblyInventory().readNbtList(nbt.getList(DISASSEMBLY_INVENTORY_NBT_KEY, NbtElement.COMPOUND_TYPE));
		this.getResultInventory().readNbtList(nbt.getList(RESULT_INVENTORY_NBT_KEY, NbtElement.COMPOUND_TYPE));
		this.inputItemConsumed = nbt.contains(INPUT_CONSUMED_NBT_KEY) ? nbt.getBoolean(INPUT_CONSUMED_NBT_KEY) : false;
		this.expectedResultCount = nbt.contains(EXPECTED_RESULT_COUNT_NBT_KEY) ? nbt.getInt(EXPECTED_RESULT_COUNT_NBT_KEY) : 0;

		this.markDirtyAndUpdateListeners();
	}

	// Provide methods to update the state from the screen handler
	public void setInputItemConsumed(boolean consumed) {
		if (this.inputItemConsumed != consumed) {
			this.inputItemConsumed = consumed;
			// Make sure to update the client state immediately
			this.markDirtyAndUpdateListeners();
		}
	}

	public void setExpectedResultCount(int count) {
		if (this.expectedResultCount != count) {
			this.expectedResultCount = count;
			this.markDirtyAndUpdateListeners();
		}
	}

	public boolean isInputItemConsumed() {
		return this.inputItemConsumed;
	}

	public int getExpectedResultCount() {
		return this.expectedResultCount;
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		// Create a proper NbtCompound with all the block entity data
		NbtCompound nbt = new NbtCompound();
		this.writeNbt(nbt);
		return nbt;
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
		// Update both the block and the blockstate to ensure clients get latest data
		this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
	}

	// Item getters for rendering
	public ItemStack getRenderInventory() {
		return this.disassemblyInventory.getStack(0);
	}

	public ItemStack getRenderResultSlot1() {
		return this.resultInventory.getStack(0);
	}

	public ItemStack getRenderResultSlot2() {
		return this.resultInventory.getStack(1);
	}

	public ItemStack getRenderResultSlot3() {
		return this.resultInventory.getStack(2);
	}

	public ItemStack getRenderResultSlot4() {
		return this.resultInventory.getStack(3);
	}

	public ItemStack getRenderResultSlot5() {
		return this.resultInventory.getStack(4);
	}

	public ItemStack getRenderResultSlot6() {
		return this.resultInventory.getStack(5);
	}

	public ItemStack getRenderResultSlot7() {
		return this.resultInventory.getStack(6);
	}

	public ItemStack getRenderResultSlot8() {
		return this.resultInventory.getStack(7);
	}

	public ItemStack getRenderResultSlot9() {
		return this.resultInventory.getStack(8);
	}

	/**
	 * Resets the block entity state when all items have been processed
	 */
	public void resetState() {
		// Only reset if both conditions are met to avoid flickering in the UI
		if (this.inputItemConsumed && this.resultInventory.isEmpty()) {
			this.inputItemConsumed = false;
			this.expectedResultCount = 0;
			this.markDirtyAndUpdateListeners();
		}
	}
}
