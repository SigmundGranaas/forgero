package com.sigmundgranaas.forgero.minecraft.common.registry.entity.block;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock.UPGRADE_STATION;
import static com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock.UPGRADE_STATION_BLOCK;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.entity.UpgradeStationBlockEntity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;

public class BlockEntityRegistry {
	public static BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY;
	public static BlockEntityType<UpgradeStationBlockEntity> UPGRADE_STATION_BLOCK_ENTITY;

	public static void register() {
		ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE, ASSEMBLY_STATION_IDENTIFIER, BlockEntityType.Builder.create(
						AssemblyStationBlockEntity::new,
						ASSEMBLY_STATION_BLOCK
				).build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, ASSEMBLY_STATION_NAME))
		);

		UPGRADE_STATION_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE, UPGRADE_STATION, BlockEntityType.Builder.create(
						UpgradeStationBlockEntity::new,
						UPGRADE_STATION_BLOCK
				).build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, "upgrade_station"))
		);
	}
}
