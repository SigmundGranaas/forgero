package com.sigmundgranaas.forgero.smithingrework;


import static com.sigmundgranaas.forgero.smithingrework.ForgeroSmithingInitializer.BLOOMERY_SCREEN_HANDLER;

import com.sigmundgranaas.forgero.smithingrework.block.entity.BloomeryScreen;
import com.sigmundgranaas.forgero.smithingrework.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithingrework.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithingrework.fluid.ModFluids;
import com.sigmundgranaas.forgero.smithingrework.networking.ModMessages;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(BLOOMERY_SCREEN_HANDLER, BloomeryScreen::new);

		BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);

		ModMessages.registerS2CPackets();

		FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.STILL_MOLTEN_IRON, ModFluids.FLOWING_MOLTEN_IRON, new SimpleFluidRenderHandler(
				new Identifier("forgero:block/fluid_still"),
				new Identifier("forgero:block/fluid_flow")
		));
	}
}
