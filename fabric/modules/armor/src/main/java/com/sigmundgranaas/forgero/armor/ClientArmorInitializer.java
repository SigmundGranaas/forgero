package com.sigmundgranaas.forgero.armor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.armor.client.RuntimeTextureWriter;
import com.sigmundgranaas.forgero.armor.client.model.ForgeroModelProvider;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
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
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientArmorInitializer implements ClientModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(ClientArmorInitializer.class);
	public static final RuntimeResourcePack RRP = RuntimeResourcePack.create(MOD_NAMESPACE + ":armor_resources");

	public static ModelRegistry modelRegistry;

	@Override
	public void onInitializeClient() {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero Armor client initialization.");

		// 1. Load the data bundle to get component and model information
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();

		// 2. Initialize the model generation pipeline
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider, MOD_NAMESPACE);

		// 3. Create a model registry and run the initializer to populate it
		modelRegistry = new MapBackedModelRegistry();
		ModelInitializationResult initResult = modelInitializer.initialize(
				bundle.componentRegistry().all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
				bundle.tagGraph(),
				modelRegistry
		);

		// 4. Generate individual part textures for our dynamic models
		List<TextureGenerationTask> tasks = initResult.generationResult().textureGenerationTasks();
		generateTextures(tasks, resourceProvider);
		generateAtlasConfig(tasks);

		// 5. Register our dynamic model provider
		ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(new ForgeroModelProvider()));

		// 6. Register the runtime resource pack with Fabric
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

		if (sources.size() == 0) {
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
