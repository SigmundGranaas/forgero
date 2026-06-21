package com.sigmundgranaas.forgero.smithing;

import static com.sigmundgranaas.forgero.smithing.block.ModBlocks.HEARTH;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.HearthBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.client.SmithingTongsWorkableParticleHandler;
import com.sigmundgranaas.forgero.smithing.client.WorkableWaxParticleFactory;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.item.renderer.MorphedItemRenderer;
import com.sigmundgranaas.forgero.smithing.item.renderer.SmithingTongsItemRenderer;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameHudOverlay;
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
		BlockEntityRendererRegistry.register(
				ModBlockEntities.SMITHING_ANVIL,
				SmithingAnvilBlockEntityRenderer::new
		);

		BlockEntityRendererRegistry.register(
				ModBlockEntities.HEARTH,
				HearthBlockEntityRenderer::new
		);

		BuiltinItemRendererRegistry.INSTANCE.register(
				ModItems.MORPHED_ITEM,
				new MorphedItemRenderer()
		);

		BuiltinItemRendererRegistry.INSTANCE.register(
				ModItems.SMITHING_TONGS,
				new SmithingTongsItemRenderer()
		);

		registerTongsModelPredicate();

		BlockRenderLayerMap.INSTANCE.putBlock(
				HEARTH,
				RenderLayer.getCutout()
		);

		ModClientMessages.registerClientPackets();
		WorkableWaxParticleFactory.register();

		HudRenderCallback.EVENT.register(new MinigameHudOverlay());

		TemperatureColorProvider.register();
		SmithingTongsWorkableParticleHandler.register();
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

//TODO
// renderer fixes for all the items
// Particles sounds/effects.
