package com.sigmundgranaas.forgero.minecraft.common.registry.entity.block;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;

public class BlockEntityRegistry implements ModInitializer {
	public static BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE, ASSEMBLY_STATION_IDENTIFIER, BlockEntityType.Builder.create(
						AssemblyStationBlockEntity::new,
						ASSEMBLY_STATION_BLOCK
				).build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, ASSEMBLY_STATION_NAME))
		);
	}
}
