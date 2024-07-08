package com.sigmundgranaas.forgero.smithingrework.block.entity;

import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.smithingrework.block.ModBlocks;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlockEntities {
    public static BlockEntityType<SmithingAnvilBlockEntity> SMITHING_ANVIL;

	public static BlockEntityType<BloomeryBlockEntity> BLOOMERY_BLOCK;

    public static void registerBlockEntities() {
        SMITHING_ANVIL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Forgero.NAMESPACE, "smithing_anvil"),
                FabricBlockEntityTypeBuilder.create(SmithingAnvilBlockEntity::new,
                        ModBlocks.SMITHING_ANVIL).build(null));
		BLOOMERY_BLOCK = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "bloomery_block"),
				FabricBlockEntityTypeBuilder.create(BloomeryBlockEntity::new,
						ModBlocks.BLOOMERY).build(null));
    }
}
