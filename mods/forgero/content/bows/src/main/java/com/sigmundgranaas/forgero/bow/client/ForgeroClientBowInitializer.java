package com.sigmundgranaas.forgero.bow.client;


import static com.sigmundgranaas.forgero.bow.ForgeroBowInitializer.DYNAMIC_ARROW_ENTITY;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ForgeroClientBowInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(DYNAMIC_ARROW_ENTITY, DynamicArrowEntityRenderer::new);
	}
}
