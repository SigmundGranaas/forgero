package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
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

public class ForgeroModelResourceListener implements IdentifiableResourceReloadListener {
	public static final Identifier ID = new Identifier("forgero", "model_reload_listener");

	// Services received from ForgeroInitializedCallback
	private static TaggedRegistry<Component> taggedComponents;
	private static TagResolver tagResolver;
	private static ComponentConverter converter;
	private static ComponentRegistry componentRegistry;

	static {
		// Use registerAndReplay to handle the case where the callback already fired
		// before this class was loaded (client initializers run after main initializers)
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

	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> {
					LOGGER.info("Hot reload detected. Reloading Forgero models...");
					ResourceProvider resourceProvider = new MinecraftResourceProvider(manager);

					ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
					ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
					ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider);

					// Check if data has been initialized yet
					if (taggedComponents == null || tagResolver == null) {
						LOGGER.warn("Forgero data not initialized during resource reload. Skipping model reload.");
						return null;
					}

					ModelInitializationResult result = modelInitializer.initialize(
							taggedComponents.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
							tagResolver,
							itemModelRegistry,
							armorModelRegistry
					);

					// Generate textures on the worker thread.
					generateTextures(result.generationResult().textureGenerationTasks(), manager);
					generateAtlasConfig(result.generationResult().textureGenerationTasks());

					ForgeroClient.services = new ForgeroClient.ClientServices(
							result.itemModelRegistry(),
							result.armorModelRegistry(),
							converter != null ? converter::toComponent : s -> java.util.Optional.empty(),
							componentRegistry,
							new ForgeroArmorTextureManager(result.itemModelRegistry()),
							new ForgeroArmorModelManager(MinecraftClient.getInstance().getEntityModelLoader())
					);
					LOGGER.info("Forgero models reloaded successfully. {} item models available.", result.itemModelRegistry().models().size());

					return result;
				}, prepareExecutor)
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(result -> {
				}, applyExecutor);
	}

	private void generateTextures(List<TextureGenerationTask> tasks, ResourceManager resourceManager) {
		if (tasks.isEmpty()) return;
		LOGGER.info("Generating {} textures at runtime...", tasks.size());
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
		LOGGER.info("Generated and added atlas configuration for {} item textures.", atlasBuilder.getSources().size());
	}
}
