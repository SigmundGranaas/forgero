package com.sigmundgranaas.forgero.smithingrework;


import com.sigmundgranaas.forgero.smithingrework.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithingrework.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithingrework.networking.ModMessages;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		ModMessages.registerS2CPackets();

	}
}
