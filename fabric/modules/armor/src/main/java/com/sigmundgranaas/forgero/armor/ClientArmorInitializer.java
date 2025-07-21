package com.sigmundgranaas.forgero.armor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.armor.client.RuntimeTextureWriter;
import com.sigmundgranaas.forgero.armor.client.model.ForgeroModelProvider;
import com.sigmundgranaas.forgero.armor.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.model.api.Model;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelDataInitializer;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelInitializationResult;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.texture.impl.AwtPalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.DefaultTextureGenerator;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientArmorInitializer implements ClientModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(ClientArmorInitializer.class);
	public static final RuntimeResourcePack RRP = RuntimeResourcePack.create(MOD_NAMESPACE + ":armor_resources");

	@Override
	public void onInitializeClient() {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero Armor client initialization.");

		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		TaggedRegistry<Component> componentRegistry = bundle.componentRegistry();

		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider, MOD_NAMESPACE);

		ModelRegistry modelRegistry = new MapBackedModelRegistry();
		ModelInitializationResult initResult = modelInitializer.initialize(
				componentRegistry.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
				bundle.tagGraph(),
				modelRegistry
		);

		// 1. Identify all items that should have a custom layered model.
		Set<Identifier> itemsToOverride = modelRegistry.models().stream()
				.filter(model -> model.getContext().isEmpty())
				.map(Model::getIdentifier)
				.map(openId -> new Identifier(openId.namespace(), openId.path()))
				.collect(Collectors.toSet());

		// 2. Create the map of baseline components for the model provider.
		Map<Identifier, Component> baselineComponentMap = itemsToOverride.stream()
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.flatMap(openId -> componentRegistry.find(openId).stream())
				.collect(Collectors.toMap(
						comp -> new Identifier(comp.id().namespace(), comp.id().path()),
						Function.identity()
				));
		LOGGER.info("Found {} items with custom models to override.", baselineComponentMap.size());


		// 3. Define the function for converting an ItemStack to a Component for dynamic overrides.
		Function<ItemStack, Optional<Component>> itemToComponentConverter = (stack) -> {
			Identifier itemId = Registries.ITEM.getId(stack.getItem());
			OpenIdentifier componentId = new OpenIdentifier(itemId.toString());
			Optional<Component> foundComponent = componentRegistry.find(componentId);
			if (foundComponent.isPresent()) {
				return foundComponent;
			}
			if (stack.getItem() instanceof ForgeroHostItem host) {
				return Optional.of(host.getForgeroComponent());
			}
			return Optional.empty();
		};

		// 4. Generate textures needed for runtime models.
		List<TextureGenerationTask> tasks = initResult.generationResult().textureGenerationTasks();
		generateTextures(tasks, resourceProvider);
		generateAtlasConfig(tasks);

		// 5. Register our model provider, injecting all necessary dependencies.
		var modelProvider = new ForgeroModelProvider(baselineComponentMap, itemToComponentConverter, modelRegistry);
		ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(modelProvider));

		// 6. Register the runtime resource pack.
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RRP));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Forgero Armor client initialization complete. Took {}ms.", endTime - startTime);
	}

	private void generateTextures(List<TextureGenerationTask> tasks, ResourceProvider resourceProvider) {
		if (tasks.isEmpty()) {
			LOGGER.info("No armor textures to generate.");
			return;
		}
		LOGGER.info("Generating {} armor textures at runtime...", tasks.size());
		var textureGenerator = new DefaultTextureGenerator(
				resourceProvider,
				new AwtPalettizedTextureGenerator(),
				new RuntimeTextureWriter(RRP)
		);
		textureGenerator.generate(tasks);
		LOGGER.info("Finished generating armor textures.");
	}

	private void generateAtlasConfig(List<TextureGenerationTask> tasks) {
		if (tasks.isEmpty()) {
			return;
		}

		JsonArray sources = new JsonArray();
		tasks.stream()
				.map(TextureGenerationTask::output)
				.distinct()
				.filter(textureId -> !textureId.contains("models/armor"))
				.forEach(textureId -> {
					JsonObject entry = new JsonObject();
					entry.addProperty("type", "single");
					entry.addProperty("resource", textureId);
					sources.add(entry);
				});

		if (sources.isEmpty()) {
			LOGGER.info("No new item textures to add to atlas.");
			return;
		}

		JsonObject atlas = new JsonObject();
		atlas.add("sources", sources);

		Identifier atlasId = new Identifier("minecraft:atlases/blocks.json");
		RRP.addAsset(atlasId, atlas.toString().getBytes());
		LOGGER.info("Generated and added atlas configuration for {} item textures.", sources.size());
	}
}
