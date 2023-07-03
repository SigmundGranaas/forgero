package com.sigmundgranaas.forgero.fabric.blockentity.assemblystation;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.EmptyHandler;
import com.sigmundgranaas.forgero.fabric.inventory.ImplementedInventory;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
	public static final Identifier IDENTIFIER = new Identifier(Forgero.NAMESPACE, "assembly_station_block_entity");
	public static final BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, FabricBlockEntityTypeBuilder.create(AssemblyStationBlockEntity::new,
					AssemblyStationBlock.ASSEMBLY_STATION_BLOCK
			).build());

	public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
	public DisassemblySlot disassemblySlot;
	public final int disassemblySlotIndex = 0;

	private DisassemblyHandler disassemblyHandler = new EmptyHandler();

	public AssemblyStationBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPos, blockState);

		disassemblySlot = new DisassemblySlot(this, 0, 34, 34, this);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return inventory;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		Inventories.writeNbt(nbt, inventory);
		super.writeNbt(nbt);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		//We provide *this* to the screenHandler as our class Implements Inventory
		//Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
		return new AssemblyStationScreenHandler(syncId, playerInventory, this);
	}

	@Override
	public Text getDisplayName() {
		// for 1.19+
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
		// for earlier versions
		// return new TranslatableText(getCachedState().getBlock().getTranslationKey());
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		ImplementedInventory.super.setStack(slot, stack);

		if (slot == disassemblySlotIndex) {
			onDisassemblySlotUpdated();
		} else {
			onSlotUpdated();
		}

		updateBlockEntityListeners();
	}

	@Override
	public ItemStack removeStack(int slot) {
		if (slot == disassemblySlotIndex) {
			onItemRemovedFromDisassemblySlot();
		} else {
			onSlotUpdated();
		}

		updateBlockEntityListeners();
		return ImplementedInventory.super.removeStack(slot);
	}

	public void onDisassemblySlotUpdated() {
		boolean isEmpty = getStack(0).isEmpty();
		var itemsToCompare = new ArrayList<>(getItems().stream().toList());
		itemsToCompare.remove(disassemblySlotIndex);
		itemsToCompare.removeIf(itemStack -> itemStack == ItemStack.EMPTY);
		disassemblySlot.doneConstructing();

		if (isEmpty && disassemblyHandler.isDisassembled(itemsToCompare)) {
			onItemRemovedFromDisassemblySlot();
		} else if (!isEmpty) {
			onItemAddedToDisassemblySlot();
		}
	}

	/**
	 * Update the block entity listeners so the inventory gets updated on the client.
	 * This is done because the item renderer needs to access the inventory clientside.
	 */
	private void updateBlockEntityListeners() {
		if (this.world != null) {
			var state = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, state, state, Block.NOTIFY_LISTENERS);
		}
	}

	private void onItemAddedToDisassemblySlot() {
		disassemblySlot.addToolToDisassemblySlot();
		this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
		var items = disassemblyHandler.disassemble();

		for (int i = 1; i < items.size() + 1; i++) {
			var element = items.get(i - 1);
			setStack(i, element);
		}

		disassemblySlot.doneConstructing();
	}

	private void onItemRemovedFromDisassemblySlot() {
		disassemblySlot.removeTool();
		inventory.clear();
		this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
	}

	public void onSlotUpdated() {
		if (disassemblySlot.doneConstructing) {
			if (!disassemblySlot.isEmpty()) {
				disassemblySlot.removeTool();
				disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
			}
		}
	}

	public static class DisassemblySlot extends Slot {
		private final Inventory resultInventory;
		private boolean doneConstructing = true;

		public DisassemblySlot(Inventory inventory, int index, int x, int y, Inventory craftingInventory) {
			super(inventory, index, x, y);
			this.resultInventory = craftingInventory;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		public boolean isEmpty() {
			return inventory.isEmpty();
		}

		public void addToolToDisassemblySlot() {
			this.doneConstructing = false;
		}

		public void removeTool() {
			this.doneConstructing = true;
			if (!this.inventory.getStack(0).isEmpty()) {
				this.inventory.getStack(0).decrement(1);
			}
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return this.inventory.isEmpty() && doneConstructing && stack.getDamage() == 0 && resultInventory.isEmpty() && !(DisassemblyHandler.createHandler(
					stack) instanceof EmptyHandler);
		}

		public void doneConstructing() {
			this.doneConstructing = true;
		}
	}
}
