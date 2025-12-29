package com.sigmundgranaas.forgero.blocks.assembly;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource reload listener for disassembly recipes.
 * <p>
 * This listener is registered with Fabric's ResourceManagerHelper to reload
 * JSON disassembly recipes whenever resource packs are reloaded.
 */
public class DisassemblyRecipeReloadListener implements SimpleSynchronousResourceReloadListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisassemblyRecipeReloadListener.class);
	public static final Identifier ID = new Identifier("forgero", "disassembly_recipes");

	@Override
	public void reload(ResourceManager manager) {
		LOGGER.info("Reloading disassembly recipes...");

		try {
			DisassemblyRecipeLoader.reload(manager);
			int count = DisassemblyRecipeLoader.getRecipes().size();
			LOGGER.info("Loaded {} disassembly recipe(s)", count);
		} catch (Exception e) {
			LOGGER.error("Failed to reload disassembly recipes", e);
		}
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}
}
