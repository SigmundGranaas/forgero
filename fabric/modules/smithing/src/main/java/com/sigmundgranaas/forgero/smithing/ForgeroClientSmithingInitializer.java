package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {


		BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);

		ModMessages.registerS2CPackets();

	}
}
