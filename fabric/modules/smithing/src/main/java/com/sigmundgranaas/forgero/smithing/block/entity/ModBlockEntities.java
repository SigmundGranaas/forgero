package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public final class ModBlockEntities {
	public static BlockEntityType<SmithingAnvilBlockEntity> SMITHING_ANVIL;
	public static BlockEntityType<HearthBlockEntity> HEARTH;

	private ModBlockEntities() {
	}

	public static void registerBlockEntities() {
		SMITHING_ANVIL = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "smithing_anvil"),
				FabricBlockEntityTypeBuilder.create(
						SmithingAnvilBlockEntity::new,
						Blocks.ANVIL,
						Blocks.CHIPPED_ANVIL,
						Blocks.DAMAGED_ANVIL
				).build(null)
		);

		HEARTH = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "hearth"),
				FabricBlockEntityTypeBuilder.create(
						HearthBlockEntity::new,
						ModBlocks.HEARTH
				).build(null)
		);
	}
}
