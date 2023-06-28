package com.sigmundgranaas.forgero.fabric.blockentity.assemblystation;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class AssemblyStationBlockEntity extends BlockEntity {
	public static final Identifier IDENTIFIER = new Identifier(Forgero.NAMESPACE, "assembly_station_block_entity");
	public static final BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, FabricBlockEntityTypeBuilder.create(AssemblyStationBlockEntity::new,
					AssemblyStationBlock.ASSEMBLY_STATION_BLOCK
			).build());

	public AssemblyStationBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPos, blockState);
	}
}
