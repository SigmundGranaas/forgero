package com.sigmundgranaas.forgero.bows.client;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side initialization for the bow module.
 * Registers entity renderers that are only available on the client.
 */
@Environment(EnvType.CLIENT)
public class BowClientPlugin implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Register DynamicArrowEntity renderer
		EntityRendererRegistry.register(
				DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				DynamicArrowEntityRenderer::new
		);
	}
}
