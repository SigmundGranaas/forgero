package com.sigmundgranaas.forgero.fabric.blockentity.assemblystation;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.fabric.inventory.ImplementedInventory;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
	public static final Identifier IDENTIFIER = new Identifier(Forgero.NAMESPACE, "assembly_station_block_entity");
	public static final BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, FabricBlockEntityTypeBuilder.create(
					AssemblyStationBlockEntity::new,
					AssemblyStationBlock.ASSEMBLY_STATION_BLOCK
			).build());

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(10, ItemStack.EMPTY);

	public AssemblyStationBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPos, blockState);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, items);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		Inventories.writeNbt(nbt, items);
		super.writeNbt(nbt);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		for (ItemStack stack : items) {
			Forgero.LOGGER.info(stack.getName());
		}

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
}
