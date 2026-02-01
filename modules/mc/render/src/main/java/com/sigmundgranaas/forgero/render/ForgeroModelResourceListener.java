package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
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
import com.sigmundgranaas.forgero.render.texture.RuntimeTextureWriter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.render.RenderInitializer.LOGGER;

/**
 * Resource reload listener that generates Forgero models synchronously during the prepare phase.
 * 
 * This ensures models are available in the registry BEFORE Minecraft starts loading models,
 * preventing the "Unable to load model" warnings that occur when models are generated asynchronously.
 * 
 * The flow is:
 * 1. PREPARE (synchronous): Generate all Forgero models and populate ForgeroClient.services
 * 2. MODEL LOADING (synchronous): Minecraft loads models, ForgeroModelProvider finds them in registry
 * 3. APPLY (asynchronous): Generate textures (can be done async as it doesn't affect model loading)
 */
public class ForgeroModelResourceListener implements IdentifiableResourceReloadListener {
	public static final Identifier ID = new Identifier("forgero", "model_reload_listener");

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
	public Identifier getFabricId() {
		return ID;
	}

	/**
	 * Performs synchronous model generation during resource reload.
	 * 
	 * This method is called during the prepare phase, which happens BEFORE model loading.
	 * We generate all models here so they're available when Minecraft asks for them.
	 */
	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		// STEP 1: Synchronous model generation (prepare phase)
		// This MUST complete before model loading starts
		CompletableFuture<ModelInitializationResult> modelsFuture = CompletableFuture.supplyAsync(() -> {
			prepareProfiler.startTick();
			prepareProfiler.push("forgero:generate_models");
			
			LOGGER.debug("Generating Forgero models...");
			ResourceProvider resourceProvider = new MinecraftResourceProvider(manager);

			ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
			ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
			ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider);

			if (taggedComponents == null || tagResolver == null) {
				LOGGER.warn("Forgero data not initialized during resource reload - skipping model generation.");
				prepareProfiler.pop();
				prepareProfiler.endTick();
				return null;
			}

			ModelInitializationResult result = modelInitializer.initialize(
					taggedComponents.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
					tagResolver,
					itemModelRegistry,
					armorModelRegistry
			);

			// CRITICAL: Populate services BEFORE model loading starts
			ForgeroClient.services = new ForgeroClient.ClientServices(
					result.itemModelRegistry(),
					result.armorModelRegistry(),
					converter != null ? converter::toComponent : s -> java.util.Optional.empty(),
					componentRegistry,
					new ForgeroArmorTextureManager(result.itemModelRegistry()),
					new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
			);
			
			LOGGER.debug("Forgero models generated: {} item models", result.itemModelRegistry().models().size());
			
			prepareProfiler.pop();
			prepareProfiler.endTick();
			return result;
		}, prepareExecutor);

		// STEP 2: Wait for prepare to complete before allowing model loading
		// The synchronizer ensures model loading doesn't start until we're done
		return modelsFuture
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(result -> {
					// STEP 3: Asynchronous texture generation (apply phase)
					// This can happen after model loading since textures are loaded separately
					if (result != null) {
						applyProfiler.startTick();
						applyProfiler.push("forgero:generate_textures");
						
						generateTextures(result.generationResult().textureGenerationTasks(), manager);
						generateAtlasConfig(result.generationResult().textureGenerationTasks());
						
						applyProfiler.pop();
						applyProfiler.endTick();
					}
				}, applyExecutor);
	}

	private void generateTextures(List<TextureGenerationTask> tasks, ResourceManager resourceManager) {
		if (tasks.isEmpty()) return;
		LOGGER.debug("Generating {} textures at runtime", tasks.size());

		// Clear the resource pack before regenerating textures to prevent double-loading
		// This is necessary because the resource pack persists across reloads (F3+T)
		// and we don't want to accumulate duplicate textures
		RenderInitializer.getResourcePack().clear();

		var textureGenerator = new DefaultTextureGenerator(new MinecraftResourceProvider(resourceManager), new AwtPalettizedTextureGenerator(), new RuntimeTextureWriter(RenderInitializer.getResourcePack()));
		textureGenerator.generate(tasks);
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

		RenderInitializer.getResourcePack().addAtlas(new Identifier("minecraft", "blocks"), atlasBuilder);
		LOGGER.debug("Generated atlas config for {} item textures", atlasBuilder.getSources().size());
	}
}
