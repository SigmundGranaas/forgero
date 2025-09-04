package com.sigmundgranaas.forgero.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
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
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.render.RenderInitializer.LOGGER;
import static com.sigmundgranaas.forgero.render.RenderInitializer.RRP;

public class ForgeroModelResourceListener implements IdentifiableResourceReloadListener {
	public static final Identifier ID = new Identifier("forgero", "model_reload_listener");

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

					ModelInitializationResult result = modelInitializer.initialize(
							ForgeroApi.components().all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
							ForgeroApi.tagGraph(),
							itemModelRegistry,
							armorModelRegistry
					);

					// Generate textures on the worker thread.
					generateTextures(result.generationResult().textureGenerationTasks(), manager);
					generateAtlasConfig(result.generationResult().textureGenerationTasks());

					ForgeroClient.services = new ForgeroClient.ClientServices(
							result.itemModelRegistry(),
							result.armorModelRegistry(),
							ForgeroApi.converter()::toComponent,
							ForgeroApi.defaultComponents(),
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
		var textureGenerator = new DefaultTextureGenerator(new MinecraftResourceProvider(resourceManager), new AwtPalettizedTextureGenerator(), new RuntimeTextureWriter(RRP));
		textureGenerator.generate(tasks);
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
		LOGGER.info("Generated and added atlas configuration for {} item textures.", sources.size());
	}
}
