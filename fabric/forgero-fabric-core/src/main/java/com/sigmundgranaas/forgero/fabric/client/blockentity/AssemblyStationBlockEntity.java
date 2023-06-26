package com.sigmundgranaas.forgero.fabric.client.blockentity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class AssemblyStationBlockEntity extends LootableContainerBlockEntity {
	public static final Identifier IDENTIFIER = new Identifier(Forgero.NAMESPACE, "assembly_station_block_entity");
	public static final BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, FabricBlockEntityTypeBuilder.create(AssemblyStationBlockEntity::new,
					AssemblyStationBlock.ASSEMBLY_STATION_BLOCK
			).build());

	public AssemblyStationBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPos, blockState);
	}

	@Override
	protected DefaultedList<ItemStack> getInvStackList() {
		return ((AssemblyStationBlock) this.getCachedState().getBlock());
	}

	@Override
	protected void setInvStackList(DefaultedList<ItemStack> list) {

	}

	@Override
	protected Text getContainerName() {
		return null;
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}
}
