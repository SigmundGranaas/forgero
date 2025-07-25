package com.sigmundgranaas.forgero.smithing.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlockEntities {
	public static BlockEntityType<SmithingAnvilBlockEntity> SMITHING_ANVIL;

	public static BlockEntityType<BloomeryBlockEntity> BLOOMERY;

	public static BlockEntityType<BloomeryExtensionBlockEntity> BLOOMERY_EXTENSION;

	public static BlockEntityType<BellowsBlockEntity> BELLOWS;

	public static BlockEntityType<MoldBlockEntity> MOLD;

	// Track all mold blocks to potentially recreate the block entity type
	private static final List<Block> moldBlocks = new ArrayList<>();

	public static void registerBlockEntities() {
		SMITHING_ANVIL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "smithing_anvil"),
				FabricBlockEntityTypeBuilder.create(SmithingAnvilBlockEntity::new,
						ModBlocks.SMITHING_ANVIL).build(null));

		BLOOMERY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "bloomery"),
				FabricBlockEntityTypeBuilder.create(BloomeryBlockEntity::new,
						ModBlocks.BLOOMERY).build(null));

		BLOOMERY_EXTENSION = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "bloomery_extension"),
				FabricBlockEntityTypeBuilder.create(BloomeryExtensionBlockEntity::new,
						ModBlocks.BLOOMERY_EXTENSION).build(null));

		BELLOWS = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Forgero.NAMESPACE, "bellows"),
				FabricBlockEntityTypeBuilder.create(BellowsBlockEntity::new,
						ModBlocks.BELLOWS).build(null));

		// Initialize MOLD BlockEntityType with any already registered mold blocks
		rebuildMoldBlockEntityType();
	}

	/**
	 * Registers a new mold block to use the MoldBlockEntity type.
	 * This method should be called for every mold block, including those generated at runtime.
	 * It only adds the block to the list; call rebuildMoldBlockEntityType() after all molds are registered.
	 * @param moldBlock The mold block to register
	 */
	public static void registerMoldBlock(Block moldBlock) {
		if (!moldBlocks.contains(moldBlock)) {
			moldBlocks.add(moldBlock);
		}
	}

	/**
	 * (Re)registers the MOLD BlockEntityType with all currently registered mold blocks.
	 * Call this after all molds have been registered.
	 */
	public static void rebuildMoldBlockEntityType() {
		if (!moldBlocks.isEmpty()) {
			Block[] blockArray = moldBlocks.toArray(new Block[0]);
			try {
				// If MOLD is already registered, unregister it to avoid conflicts
				if (MOLD != null && Registries.BLOCK_ENTITY_TYPE.containsId(new Identifier(Forgero.NAMESPACE, "mold"))) {
					Forgero.LOGGER.info("MOLD BlockEntityType already exists, attempting to replace it");
				}

				MOLD = Registry.register(Registries.BLOCK_ENTITY_TYPE,
						new Identifier(Forgero.NAMESPACE, "mold"),
						FabricBlockEntityTypeBuilder.create(MoldBlockEntity::new, blockArray).build(null));
				Forgero.LOGGER.info("(Re)registered mold block entity type for {} blocks", blockArray.length);
			} catch (Exception e) {
				Forgero.LOGGER.error("Failed to register MOLD BlockEntityType: {}", e.getMessage(), e);
			}
		} else {
			Forgero.LOGGER.warn("Cannot register MOLD BlockEntityType: no mold blocks registered");
		}

		// Double-check registration was successful
		if (MOLD == null) {
			Forgero.LOGGER.error("MOLD BlockEntityType is still null after attempted registration");
		}
	}
}
