package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
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
import com.sigmundgranaas.forgero.loader.impl.FabricResourceProvider;
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Client-side render initializer for Forgero.
 *
 * This class handles model and texture generation during client initialization.
 * The key insight is that generation must happen in onInitializeClient() (not in
 * deferred callbacks) to leverage Fabric's guarantee that client entrypoints run
 * AFTER main entrypoints, ensuring data is available via ForgeroInitializedCallback.
 */
@Environment(EnvType.CLIENT)
public class RenderInitializer implements ClientModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(RenderInitializer.class);
	private static DynamicResourcePack resourcePack;

	public static DynamicResourcePack getResourcePack() {
		if (resourcePack == null) {
			resourcePack = DRPApi.getInstance()
					.createPack(MOD_NAMESPACE + ":render_resources")
					.description("Forgero render-generated resources")
					.build();
		}
		return resourcePack;
	}

	private static TaggedRegistry<Component> taggedComponents;
	private static TagResolver tagResolver;
	private static ComponentConverter converter;
	private static ComponentRegistry componentRegistry;

	static {
		ForgeroInitializedCallback.registerAndReplay(services -> {
			taggedComponents = services.taggedComponents();
			tagResolver = services.tagResolver();
			converter = services.converter();
			componentRegistry = services.componentRegistry();
		});
	}

	@Override
	public void onInitializeClient() {
		DRPApi.getInstance().register(getResourcePack(), ResourcePackPhase.BEFORE_VANILLA);

		// Generate models and textures immediately in onInitializeClient()
		// CRITICAL: This runs AFTER ForgeroDataLoader.onInitialize() (Fabric guarantee),
		// ensuring taggedComponents and tagResolver are available via the static initializer's
		// ForgeroInitializedCallback.registerAndReplay() mechanism.
		LOGGER.info("Generating Forgero models and textures...");
		generateModelsForLoading();

		// Register the Model Loading Plugin to provide the model resolver
		// The resolver is called later when Minecraft loads individual models
		LOGGER.info("Registering ModelLoadingPlugin...");
		ModelLoadingPlugin.register(pluginContext -> {
			LOGGER.debug("ModelLoadingPlugin callback invoked - registering model resolver");
			pluginContext.resolveModel().register(new ForgeroModelProvider());
		});

		// Register resource reload listener for hot reloads (F3+T)
		// This handles texture regeneration when resources are reloaded
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ForgeroModelResourceListener());
	}

	/**
	 * Synchronously generates Forgero models and textures during client initialization.
	 *
	 * This method is called from onInitializeClient(), which runs AFTER the main entrypoint
	 * (ForgeroDataLoader.onInitialize()). This timing guarantee ensures that taggedComponents
	 * and tagResolver are available via ForgeroInitializedCallback.registerAndReplay().
	 *
	 * Models and textures are generated BEFORE ModelLoadingPlugin callbacks execute,
	 * preventing "Unable to load model" warnings.
	 */
	private void generateModelsForLoading() {
		LOGGER.info("Generating Forgero models and textures during client initialization...");

		if (taggedComponents == null || tagResolver == null) {
			LOGGER.warn("Forgero data not initialized (taggedComponents={}, tagResolver={}) - skipping model generation",
				taggedComponents, tagResolver);
			return;
		}

		try {
			ResourceProvider resourceProvider;
			ResourceManager mcResourceManager = MinecraftClient.getInstance().getResourceManager();
			if (mcResourceManager != null && !mcResourceManager.getAllNamespaces().isEmpty()) {
				resourceProvider = new MinecraftResourceProvider(mcResourceManager);
			} else {
				resourceProvider = new FabricResourceProvider("assets");
			}

			ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
			ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
			ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider);

			ModelInitializationResult result = modelInitializer.initialize(
					taggedComponents.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
					tagResolver,
					itemModelRegistry,
					armorModelRegistry
			);

			// Generate textures synchronously (required for model loading)
			var tasks = result.generationResult().textureGenerationTasks();
			if (!tasks.isEmpty()) {
				// Clear any existing textures to prevent accumulation across reloads
				getResourcePack().clear();

				var textureGenerator = new DefaultTextureGenerator(resourceProvider, new AwtPalettizedTextureGenerator(), new RuntimeTextureWriter(getResourcePack()));
				textureGenerator.generate(tasks);
				generateAtlasConfig(tasks);
			}

			// Populate services BEFORE model loading starts
			ForgeroClient.services = new ForgeroClient.ClientServices(
					result.itemModelRegistry(),
					result.armorModelRegistry(),
					converter != null ? converter::toComponent : s -> Optional.empty(),
					componentRegistry,
					new ForgeroArmorTextureManager(result.itemModelRegistry()),
					new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
			);
			
			LOGGER.debug("Forgero models generated: {} item models", result.itemModelRegistry().models().size());
		} catch (Exception e) {
			LOGGER.error("Failed to generate Forgero models during model loading", e);
		}
	}

	private void generateAtlasConfig(List<TextureGenerationTask> tasks) {
		if (tasks.isEmpty()) return;

		AtlasBuilder atlasBuilder = AtlasBuilder.create();
		tasks.stream()
				.map(TextureGenerationTask::output)
				.distinct()
				.filter(textureId -> textureId.startsWith("forgero:item/"))
				.forEach(atlasBuilder::addSingle);

		if (atlasBuilder.getSources().isEmpty()) return;

		getResourcePack().addAtlas(new Identifier("minecraft", "blocks"), atlasBuilder);
	}
}
