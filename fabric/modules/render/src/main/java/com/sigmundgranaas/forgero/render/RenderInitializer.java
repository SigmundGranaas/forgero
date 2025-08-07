package com.sigmundgranaas.forgero.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.loader.ForgeroDataLoader;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.render.model.item.ForgeroModelProvider;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelDataInitializer;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelInitializationResult;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.DefaultArmorModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.impl.DefaultModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.texture.impl.AwtPalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.DefaultTextureGenerator;
import com.sigmundgranaas.forgero.render.texture.RuntimeTextureWriter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class RenderInitializer implements ClientModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(RenderInitializer.class);
	public static final RuntimeResourcePack RRP = RuntimeResourcePack.create(MOD_NAMESPACE + ":resources");

	@Override
	public void onInitializeClient() {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero client render initialization.");

		// Get the fully loaded data context from the loader module.
		DataLoadingContext context = ForgeroDataLoader.getContext();
		TaggedRegistry<Component> componentRegistry = context.getTaggedComponentRegistry();

		// Populate ForgeroClient with the globally accessible services from the loader.
		ForgeroClient.itemToComponent = context::getComponent;

		// Initialize client-specific registries and pipelines.
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
		ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();

		// Generate models from templates using the loaded components.
		ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider, MOD_NAMESPACE);
		ModelInitializationResult initResult = modelInitializer.initialize(
				context.getComponentRegistry().all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
				context.getDataBundle().tagGraph(),
				itemModelRegistry,
				armorModelRegistry
		);

		ForgeroClient.modelRegistry = initResult.itemModelRegistry();
		ForgeroClient.armorModelRegistry = initResult.armorModelRegistry();
		LOGGER.info("Initialized {} item models and {} armor models from templates.", ForgeroClient.modelRegistry.models().size(), ForgeroClient.armorModelRegistry.findAll().size());

		// Load any manually defined models as overrides.
		loadManualModels(resourceProvider);

		// Set up Fabric's model override system for Forgero items.
		setupItemModelOverrides(ForgeroClient.modelRegistry, componentRegistry);

		// Generate and register textures at runtime.
		List<TextureGenerationTask> tasks = initResult.generationResult().textureGenerationTasks();
		generateTextures(tasks, resourceProvider);
		generateAtlasConfig(tasks);

		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RRP));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Forgero client render initialization complete. Took {}ms.", endTime - startTime);
	}

	private void loadManualModels(ResourceProvider resourceProvider) {
		ArmorModelRegistrationService manualArmorModelService = new DefaultArmorModelRegistrationService(ForgeroClient.armorModelRegistry, resourceProvider);
		manualArmorModelService.registerModels(MOD_NAMESPACE);
		LOGGER.info("Loaded manual armor models. Total armor models: {}", ForgeroClient.armorModelRegistry.findAll().size());

		DefaultModelRegistrationService manualItemModelService = new DefaultModelRegistrationService(ForgeroClient.modelRegistry, resourceProvider);
		manualItemModelService.registerModels(MOD_NAMESPACE);
		LOGGER.info("Loaded manual item models. Total item models: {}", ForgeroClient.modelRegistry.models().size());
	}


	private void setupItemModelOverrides(ItemModelRegistry modelRegistry, TaggedRegistry<Component> componentRegistry) {
		Map<Identifier, Model> itemsToOverride = modelRegistry.models().stream()
				.filter(model -> model.getContext().isEmpty())
				.collect(Collectors.toMap(model -> new Identifier(model.getIdentifier().toString()), Function.identity()));

		Map<Identifier, Component> baselineComponentMap = itemsToOverride.entrySet().stream()
				.flatMap(entry -> entry.getValue().getTarget().flatMap(componentRegistry::find).stream().map(comp -> Map.entry(entry.getKey(), comp)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		var modelProvider = new ForgeroModelProvider(baselineComponentMap, ForgeroClient.itemToComponent, modelRegistry);
		ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(modelProvider));
	}

	private void generateTextures(List<TextureGenerationTask> tasks, ResourceProvider resourceProvider) {
		if (tasks.isEmpty()) {
			return;
		}
		LOGGER.info("Generating {} textures at runtime...", tasks.size());
		var textureGenerator = new DefaultTextureGenerator(
				resourceProvider,
				new AwtPalettizedTextureGenerator(),
				new RuntimeTextureWriter(RRP)
		);
		textureGenerator.generate(tasks);
	}

	private void generateAtlasConfig(List<TextureGenerationTask> tasks) {
		if (tasks.isEmpty()) {
			return;
		}

		JsonArray sources = new JsonArray();
		tasks.stream()
				.map(TextureGenerationTask::output)
				.distinct()
				.filter(textureId -> textureId.startsWith("forgero:item/"))
				.forEach(textureId -> {
					JsonObject entry = new JsonObject();
					entry.addProperty("type", "single");
					entry.addProperty("resource", "forgero:" + textureId.substring("forgero:".length()));
					sources.add(entry);
				});

		if (sources.isEmpty()) {
			return;
		}

		JsonObject atlas = new JsonObject();
		atlas.add("sources", sources);

		Identifier atlasId = new Identifier("minecraft", "atlases/blocks.json");
		RRP.addResource(ResourceType.CLIENT_RESOURCES, atlasId, atlas.toString().getBytes());
		LOGGER.info("Generated and added atlas configuration for {} item textures.", sources.size());
	}
}
