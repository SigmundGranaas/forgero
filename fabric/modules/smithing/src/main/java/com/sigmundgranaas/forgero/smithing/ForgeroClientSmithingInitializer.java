package com.sigmundgranaas.forgero.smithing;



import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.renderer.MorphedItemRenderer;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameHudOverlay;
import com.sigmundgranaas.forgero.smithing.networking.C2S.AnvilUseC2SPacket;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;


import com.sigmundgranaas.forgero.smithing.temperature.TemperatureHud;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
		BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.MORPHED_ITEM, new MorphedItemRenderer());

		ModMessages.registerS2CPackets();
		AnvilUseC2SPacket.register();
		HudRenderCallback.EVENT.register(new MinigameHudOverlay());
        TemperatureColorProvider.register();
		TemperatureHud.register();


    }
}
