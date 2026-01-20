package com.sigmundgranaas.forgero.blocks;

import com.sigmundgranaas.forgero.blocks.assembly.DisassemblyRecipeReloadListener;

import net.minecraft.resource.ResourceType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main initializer for the Forgero Blocks module.
 * <p>
 * Registers resource reload listeners and other initialization tasks
 * that need to happen during mod loading.
 */
public class BlocksInitializer implements ModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlocksInitializer.class);

	@Override
	public void onInitialize() {
		// Register disassembly recipe reload listener
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DisassemblyRecipeReloadListener());
		LOGGER.debug("Forgero Blocks initialized");
	}
}
