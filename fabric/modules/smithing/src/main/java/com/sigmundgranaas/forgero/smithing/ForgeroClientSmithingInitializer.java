package com.sigmundgranaas.forgero.smithing;

import static com.sigmundgranaas.forgero.smithing.block.ModBlocks.HEARTH;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.HearthBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.client.ClientMinigameTextureResolver;
import com.sigmundgranaas.forgero.smithing.client.SmithingTongsWorkableParticleHandler;
import com.sigmundgranaas.forgero.smithing.client.WorkableWaxParticleFactory;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.item.renderer.MorphedItemRenderer;
import com.sigmundgranaas.forgero.smithing.item.renderer.SmithingTongsItemRenderer;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameHudOverlay;
import com.sigmundgranaas.forgero.smithing.minigame.MinigamePositioning;
import com.sigmundgranaas.forgero.smithing.networking.ModClientMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerBlockEntityRenderers();
		registerItemRenderers();
		registerTongsModelPredicate();
		registerRenderLayers();
		registerHud();
		registerClientEffects();
		registerTextureResolver();
		ModClientMessages.registerClientPackets();
	}

	private void registerBlockEntityRenderers() {
		BlockEntityRendererRegistry.register(
				ModBlockEntities.SMITHING_ANVIL,
				SmithingAnvilBlockEntityRenderer::new
		);

		BlockEntityRendererRegistry.register(
				ModBlockEntities.HEARTH,
				HearthBlockEntityRenderer::new
		);
	}

	private void registerItemRenderers() {
		BuiltinItemRendererRegistry.INSTANCE.register(
				ModItems.MORPHED_ITEM,
				new MorphedItemRenderer()
		);

		BuiltinItemRendererRegistry.INSTANCE.register(
				ModItems.SMITHING_TONGS,
				new SmithingTongsItemRenderer()
		);
	}

	private void registerRenderLayers() {
		BlockRenderLayerMap.INSTANCE.putBlock(
				HEARTH,
				RenderLayer.getCutout()
		);
	}

	private void registerHud() {
		HudRenderCallback.EVENT.register(new MinigameHudOverlay());
	}

	private void registerClientEffects() {
		WorkableWaxParticleFactory.register();
		TemperatureColorProvider.register();
		SmithingTongsWorkableParticleHandler.register();
	}

	private void registerTextureResolver() {
		MinigamePositioning.setTextureResolver(new ClientMinigameTextureResolver());
	}

	@SuppressWarnings("deprecation")
	private void registerTongsModelPredicate() {
		FabricModelPredicateProviderRegistry.register(
				ModItems.SMITHING_TONGS,
				new Identifier(Forgero.NAMESPACE, "loaded"),
				(stack, world, entity, seed) -> SmithingTongsItem.hasStoredStack(stack) ? 1.0F : 0.0F
		);
	}
}
