package com.sigmundgranaas.forgero.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelDataInitializer;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelInitializationResult;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.texture.impl.AwtPalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.DefaultTextureGenerator;
import com.sigmundgranaas.forgero.render.model.armor.ForgeroArmorModelManager;
import com.sigmundgranaas.forgero.render.model.armor.ForgeroArmorTextureManager;
import com.sigmundgranaas.forgero.render.model.item.ForgeroModelProvider;
import com.sigmundgranaas.forgero.render.texture.RuntimeTextureWriter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class RenderInitializer implements ClientModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(RenderInitializer.class);
	public static final RuntimeResourcePack RRP = RuntimeResourcePack.create(MOD_NAMESPACE + ":resources");

	// Services received from ForgeroInitializedCallback
	private static TaggedRegistry<Component> taggedComponents;
	private static TagResolver tagResolver;
	private static ComponentConverter converter;
	private static ComponentRegistry componentRegistry;

	static {
		ForgeroInitializedCallback.EVENT.register(services -> {
			taggedComponents = services.taggedComponents();
			tagResolver = services.tagResolver();
			converter = services.converter();
			componentRegistry = services.componentRegistry();
		});
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Forgero client rendering setup starting...");
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RRP));

		// STEP 1: Synchronous Pre-load Phase
		// This runs once at startup, before the ModelLoader is created.
		// It uses your ClassPathResourceProvider to guarantee all model data is
		// loaded before the ModelLoadingPlugin is registered, eliminating the startup race condition.
		try {
			LOGGER.info("Starting synchronous model pre-load...");
			ResourceProvider preloadProvider = new ClassPathResourceProvider("assets");

			ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
			ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
			ModelDataInitializer modelInitializer = new ModelDataInitializer(preloadProvider);

			ModelInitializationResult initialResult = modelInitializer.initialize(
					taggedComponents.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
					tagResolver,
					itemModelRegistry,
					armorModelRegistry
			);

			// Generate initial textures and atlas config BEFORE the first resource reload
			var tasks = initialResult.generationResult().textureGenerationTasks();
			if (!tasks.isEmpty()) {
				LOGGER.info("Performing initial texture generation for {} tasks...", tasks.size());
				var textureGenerator = new DefaultTextureGenerator(preloadProvider, new AwtPalettizedTextureGenerator(), new RuntimeTextureWriter(RRP));
				textureGenerator.generate(tasks);
				generateAtlasConfig(tasks);
			}


			// Create and populate the initial services object.
			ForgeroClient.services = new ForgeroClient.ClientServices(
					initialResult.itemModelRegistry(),
					initialResult.armorModelRegistry(),
					converter::toComponent,
					componentRegistry,
					new ForgeroArmorTextureManager(initialResult.itemModelRegistry()),
					new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
			);
			LOGGER.info("Synchronous model pre-load complete. {} item models loaded.", initialResult.itemModelRegistry().models().size());
		} catch (Exception e) {
			LOGGER.error("Critical error during Forgero synchronous pre-load. Models will not render correctly.", e);
			// To prevent crashes in a broken state, initialize with empty services.
			ForgeroClient.services = new ForgeroClient.ClientServices(new MapBackedModelRegistry(), new MapBackedArmorModelRegistry(), s -> Optional.empty(), null, null, null);
		}

		// STEP 2: Register Live-Reload Systems
		// These systems will take over for hot reloads (F3+T), using Minecraft's resource manager.
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ForgeroModelResourceListener());

		// STEP 3: Register the Model Provider
		// This is now 100% safe because the synchronous pre-load has already populated ForgeroClient.services.
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.resolveModel().register(new ForgeroModelProvider());
			LOGGER.info("Forgero ModelLoadingPlugin registered.");
		});
	}

	private void generateAtlasConfig(List<TextureGenerationTask> tasks) {
		if (tasks.isEmpty()) return;
		JsonArray sources = new JsonArray();
		tasks.stream().map(TextureGenerationTask::output).distinct().filter(textureId -> textureId.startsWith("forgero:item/")).forEach(textureId -> {
			JsonObject entry = new JsonObject();
			entry.addProperty("type", "single");
			entry.addProperty("resource", textureId);
			sources.add(entry);
		});
		if (sources.isEmpty()) return;
		JsonObject atlas = new JsonObject();
		atlas.add("sources", sources);
		Identifier atlasId = new Identifier("minecraft", "atlases/blocks.json");
		RRP.addResource(ResourceType.CLIENT_RESOURCES, atlasId, atlas.toString().getBytes());
		LOGGER.info("Generated and added initial atlas configuration for {} item textures.", sources.size());
	}
}
