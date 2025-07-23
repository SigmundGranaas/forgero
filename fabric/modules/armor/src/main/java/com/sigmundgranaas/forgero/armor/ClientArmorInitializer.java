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
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelDataInitializer;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelInitializationResult;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.DefaultArmorModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedArmorModelRegistry;
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

		// DATA LOADING
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		TaggedRegistry<Component> componentRegistry = bundle.componentRegistry();

		// Set up the simple ItemStack to Component converter
		ForgeroClient.itemToComponent = (stack) -> {
			if (stack.getItem() instanceof ForgeroHostItem host) {
				// For now, we only care about the baseline component, not NBT data.
				return Optional.of(host.getForgeroComponent());
			}
			return Optional.empty();
		};

		// RESOURCE PROVIDER
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");

		// ITEM MODEL INITIALIZATION
		ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider, MOD_NAMESPACE);
		ItemModelRegistry itemModelRegistry = new MapBackedModelRegistry();
		ModelInitializationResult initResult = modelInitializer.initialize(
				componentRegistry.all().stream().collect(Collectors.toMap(Component::id, Function.identity())),
				bundle.tagGraph(),
				itemModelRegistry
		);
		ForgeroClient.modelRegistry = itemModelRegistry;

		// ARMOR MODEL INITIALIZATION
		MapBackedArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();
		ArmorModelRegistrationService armorModelRegistrationService = new DefaultArmorModelRegistrationService(armorModelRegistry, resourceProvider);
		armorModelRegistrationService.registerModels(MOD_NAMESPACE);
		ForgeroClient.armorModelRegistry = armorModelRegistry;
		LOGGER.info("Loaded {} custom armor models.", armorModelRegistry.findAll().size());

		// ITEM MODEL OVERRIDE SETUP
		setupItemModelOverrides(itemModelRegistry, componentRegistry);

		// TEXTURE GENERATION
		List<TextureGenerationTask> tasks = initResult.generationResult().textureGenerationTasks();
		generateTextures(tasks, resourceProvider);
		generateAtlasConfig(tasks);

		// REGISTER RUNTIME RESOURCE PACK
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RRP));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Forgero Armor client initialization complete. Took {}ms.", endTime - startTime);
	}

	private void setupItemModelOverrides(ItemModelRegistry modelRegistry, TaggedRegistry<Component> componentRegistry) {
		Set<Identifier> itemsToOverride = modelRegistry.models().stream()
				.filter(model -> model.getContext().isEmpty())
				.map(Model::getIdentifier)
				.map(openId -> new Identifier(openId.namespace(), openId.path()))
				.collect(Collectors.toSet());

		Map<Identifier, Component> baselineComponentMap = itemsToOverride.stream()
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.flatMap(openId -> componentRegistry.find(openId).stream())
				.collect(Collectors.toMap(
						comp -> new Identifier(comp.id().namespace(), comp.id().path()),
						Function.identity()
				));

		var modelProvider = new ForgeroModelProvider(baselineComponentMap, ForgeroClient.itemToComponent, modelRegistry);
		ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(modelProvider));
	}

	private void generateTextures(List<TextureGenerationTask> tasks, ResourceProvider resourceProvider) {
		if (tasks.isEmpty()) {
			return;
		}
		LOGGER.info("Generating {} armor textures at runtime...", tasks.size());
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
					entry.addProperty("resource", textureId);
					sources.add(entry);
				});

		if (sources.isEmpty()) {
			return;
		}

		JsonObject atlas = new JsonObject();
		atlas.add("sources", sources);

		Identifier atlasId = new Identifier("minecraft:atlases/blocks.json");
		RRP.addAsset(atlasId, atlas.toString().getBytes());
		LOGGER.info("Generated and added atlas configuration for {} item textures.", sources.size());
	}
}
