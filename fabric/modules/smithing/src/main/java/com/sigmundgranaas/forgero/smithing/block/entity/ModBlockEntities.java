package com.sigmundgranaas.forgero.smithing.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlockEntities {
	public static BlockEntityType<SmithingAnvilBlockEntity> SMITHING_ANVIL;

	private static final List<Block> moldBlocks = new ArrayList<>();

	public static void registerBlockEntities() {
		SMITHING_ANVIL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "smithing_anvil"),
				FabricBlockEntityTypeBuilder.create(SmithingAnvilBlockEntity::new,
						Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.ANVIL)
						.build(null));

	}
}
