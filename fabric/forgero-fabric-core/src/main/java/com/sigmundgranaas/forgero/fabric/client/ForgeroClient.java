package com.sigmundgranaas.forgero.fabric.client;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.client.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.fabric.client.texture.Generator;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreen;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationScreen;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableItemRenderer;
import net.devtech.arrp.api.RRPCallback;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.fabric.client.SoulEntityModel.SOUL_ENTITY_MODEL_LAYER;
import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;
import static com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationScreenHandler.UPGRADE_STATION_SCREEN_HANDLER;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
	public static Map<String, PaletteTemplateModel> TEXTURES = new HashMap<>();
	public static Map<String, String> PALETTE_REMAP = new HashMap<>();

	@Override
	public void onInitializeClient() {
		initializeItemModels();
		HandledScreens.register(ASSEMBLY_STATION_SCREEN_HANDLER, AssemblyStationScreen::new);
		HandledScreens.register(UPGRADE_STATION_SCREEN_HANDLER, UpgradeStationScreen::new);
	}

	private void initializeItemModels() {
		var modelRegistry = new ModelRegistry();
		var availableDependencies = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());

		PipelineBuilder
				.builder()
				.register(FabricPackFinder.supplier())
				.data(modelRegistry.paletteListener())
				.data(modelRegistry.modelListener())
				.register(availableDependencies)
				.silent()
				.build()
				.execute();
		// TODO: Set configuration's available dependencies
		assetReloader();
		registerToolPartTextures(modelRegistry);
		var modelProvider = new ForgeroModelVariantProvider(modelRegistry);
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(variant -> modelProvider);
		EntityRendererRegistry.register(Entities.SOUL_ENTITY, SoulEntityRenderer::new);
		EntityRendererRegistry.register(Entities.THROWN_ITEM_ENTITY, ThrowableItemRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(SOUL_ENTITY_MODEL_LAYER, SoulEntityModel::getTexturedModelData);
	}

	private void registerToolPartTextures(ModelRegistry modelRegistry) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		for (State material : materials) {
			ForgeroClient.TEXTURES.put(String.format("forgero:%s-repair_kit.png", material.name()), new PaletteTemplateModel(material.name(), "repair_kit.png", 30, null, 16, null));
		}

		PALETTE_REMAP.putAll(modelRegistry.getPaletteRemapper());
		TEXTURES.putAll(modelRegistry.getTextures());
		Generator.generate();
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(Generator.RESOURCE_PACK_CLIENT));

	}

	private void assetReloader() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				TextureGenerator.getInstance(new FileService(), PALETTE_REMAP).clear();
			}

			@Override
			public Identifier getFabricId() {
				return new Identifier(Forgero.NAMESPACE, "dynamic_textures");
			}
		});
	}
}
