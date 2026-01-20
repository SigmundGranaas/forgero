package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;
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

		// STEP 1: Synchronous Pre-load Phase
		// This runs once at startup, before the ModelLoader is created.
		// Try to use Minecraft's ResourceManager if available, as it can find resources across all mod JARs.
		// Fall back to FabricResourceProvider which uses Fabric's ModContainer API to scan all mod JARs.
		try {
			ResourceProvider preloadProvider;
			ResourceManager mcResourceManager = MinecraftClient.getInstance().getResourceManager();
			if (mcResourceManager != null && !mcResourceManager.getAllNamespaces().isEmpty()) {
				preloadProvider = new MinecraftResourceProvider(mcResourceManager);
			} else {
				preloadProvider = new FabricResourceProvider("assets");
			}

			ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
			ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
			ModelDataInitializer modelInitializer = new ModelDataInitializer(preloadProvider);

			if (taggedComponents != null && tagResolver != null) {
				ModelInitializationResult initialResult = modelInitializer.initialize(
						taggedComponents.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
						tagResolver,
						itemModelRegistry,
						armorModelRegistry
				);

				// Generate initial textures and atlas config BEFORE the first resource reload
				var tasks = initialResult.generationResult().textureGenerationTasks();
				if (!tasks.isEmpty()) {
					var textureGenerator = new DefaultTextureGenerator(preloadProvider, new AwtPalettizedTextureGenerator(), new RuntimeTextureWriter(getResourcePack()));
					textureGenerator.generate(tasks);
					generateAtlasConfig(tasks);
				}

				// Create and populate the initial services object.
				ForgeroClient.services = new ForgeroClient.ClientServices(
						initialResult.itemModelRegistry(),
						initialResult.armorModelRegistry(),
						converter != null ? converter::toComponent : s -> Optional.empty(),
						componentRegistry,
						new ForgeroArmorTextureManager(initialResult.itemModelRegistry()),
						new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
				);
				LOGGER.debug("Model pre-load complete: {} models", initialResult.itemModelRegistry().models().size());
			} else {
				LOGGER.warn("Forgero data not yet initialized during client startup. Models will be loaded on first resource reload.");
				// Initialize with empty services - hot reload will populate them later
				ForgeroClient.services = new ForgeroClient.ClientServices(
						itemModelRegistry,
						armorModelRegistry,
						s -> Optional.empty(),
						null,
						new ForgeroArmorTextureManager(itemModelRegistry),
						new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
				);
			}
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
		});
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
