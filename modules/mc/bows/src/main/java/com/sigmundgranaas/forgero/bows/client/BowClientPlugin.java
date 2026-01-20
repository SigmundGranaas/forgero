package com.sigmundgranaas.forgero.bows.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class BowClientPlugin implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(BowClientPlugin.class);

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(
				DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				DynamicArrowEntityRenderer::new
		);
		LOGGER.debug("Registered DynamicArrowEntityRenderer");
	}
}
