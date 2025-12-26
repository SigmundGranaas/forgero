package com.sigmundgranaas.forgero.loader;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.codec.CofCodecs;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.attribute.AttributeManager;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.convert.ComponentConverterImpl;
import com.sigmundgranaas.forgero.common.convert.IdMapper;
import com.sigmundgranaas.forgero.common.convert.StatefulConverter;
import com.sigmundgranaas.forgero.common.convert.TypeConverter;
import com.sigmundgranaas.forgero.common.env.ForgeroEnvironment;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.item.DynamicItem;
import com.sigmundgranaas.forgero.common.item.DynamicSwordItem;
import com.sigmundgranaas.forgero.common.item.DynamicToolItem;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.recipe.ForgeroShapedRecipeSerializer;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.common.tooltip.ForgeroTooltipRenderer;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeCodec;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.core.registry.impl.MapBackedComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.loader.api.*;
import com.sigmundgranaas.forgero.loader.impl.DataLoadingContextImpl;
import com.sigmundgranaas.forgero.loader.impl.ItemRegistrar;
import com.sigmundgranaas.forgero.loader.impl.PluginRegistrationContextImpl;
import com.sigmundgranaas.forgero.loader.impl.PluginRegistry;
import com.sigmundgranaas.forgero.loader.plugin.ForgeroDefaultsPlugin;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Central data loader that orchestrates the entire Forgero data loading process.
 * This class is responsible for:
 * 1. Loading data
 * 2. Coordinating plugin registration
 * 3. Creating and managing all registries
 * 4. Processing and registering items
 * 5. Triggering post-load callbacks
 */
public class ForgeroDataLoader implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataLoader.class);
	private static final String MOD_NAMESPACE = "forgero";

	private static ForgeroDataLoader INSTANCE;

	private final PluginRegistry pluginRegistry;
	private final DataLoadingContextImpl context;
	private final Map<Identifier, Item> registeredItems;
	private boolean initialized = false;
	private TagResolver tagResolver = TagResolver.empty();

	private record DynamicItems(Item item, Item tool, Item sword) {}

	public ForgeroDataLoader() {
		this.pluginRegistry = new PluginRegistry();
		this.context = new DataLoadingContextImpl();
		this.registeredItems = new HashMap<>();
		INSTANCE = this;
	}

	@Override
	public void onInitialize() {
		if (initialized) {
			throw new IllegalStateException("ForgeroDataLoader has already been initialized!");
		}

		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero data loading process...");

		try {
			// Phase 0: Register dynamic items
			DynamicItems dynamicItems = registerDynamicItems();

			// Phase 1: Collect all plugins
			collectPlugins();

			// Phase 2: Load the TagResolver BEFORE anything else
			this.tagResolver = loadTagResolver();

			// Phase 3: Register plugin requirements, now with a valid TagResolver
			PluginRegistrationContextImpl registrationContext = registerPluginRequirements(() -> this.tagResolver);

			// Phase 4: Create configuration for the data initializer
			ForgeroDataInitializer.Config dataConfig = createDataConfig(registrationContext, this.tagResolver);

			// Phase 5: Load data using the configuration
			ForgeroDataBundle bundle = loadData(dataConfig);

			// Phase 6: Initialize core systems
			initializeCoreServices(bundle, registrationContext, dataConfig, dynamicItems);

			// Phase 7: Setup item registration callbacks
			ItemRegistrar itemRegistrar = setupItemRegistration();

			// Phase 8: Process and register items
			List<ItemRegistrar.RegisteredItem> items = processItems(bundle, registrationContext.getItemCreators(), itemRegistrar);

			// Store registered items for lookup
			items.forEach(item -> registeredItems.put(item.id(), item.item()));

			// Phase 9: Notify post-load plugins
			notifyPostLoadPlugins();

			// Phase 10: Initialize recipe serializers
			initializeRecipes(dataConfig);


			initialized = true;
			ForgeroTooltipRenderer.initialize(context.getConverter(), context.getResolver());

			// Phase 11: Fire the initialization event for DI-based subscribers
			LOGGER.debug("Firing ForgeroInitializedCallback event...");
			ForgeroInitializedCallback.EVENT.invoker().onForgeroInitialized(context);
			LOGGER.info("ForgeroServices are now available via event subscribers.");

			// Phase 12: Finalize and expose the public API for convenience access
			ForgeroApi.initialize(context);
			LOGGER.info("Forgero Public API is now available.");

			long endTime = System.currentTimeMillis();
			LOGGER.info("Forgero data loading complete. Registered {} items in {}ms",
					registeredItems.size(), endTime - startTime);

		} catch (Exception e) {
			LOGGER.error("Critical error during Forgero data loading", e);
			throw new RuntimeException("Failed to initialize Forgero", e);
		}
	}

	private DynamicItems registerDynamicItems() {
		Item.Settings settings = new Item.Settings();
		Item dynamicItem = Registry.register(Registries.ITEM, new Identifier(MOD_NAMESPACE, "dynamic_item"), new DynamicItem(settings));
		Item dynamicToolItem = Registry.register(Registries.ITEM, new Identifier(MOD_NAMESPACE, "dynamic_tool"), new DynamicToolItem(settings));
		Item dynamicSwordItem = Registry.register(Registries.ITEM, new Identifier(MOD_NAMESPACE, "dynamic_sword"), new DynamicSwordItem(settings));
		LOGGER.info("Registered dynamic items for NBT-driven component wrapping.");
		return new DynamicItems(dynamicItem, dynamicToolItem, dynamicSwordItem);
	}

	private void collectPlugins() {
		pluginRegistry.discoverPlugins();
		// Manually register the defaults plugin
		pluginRegistry.registerPlugin(new ForgeroDefaultsPlugin());
		int dataPlugins = pluginRegistry.getDataPlugins().size();
		int itemRegPlugins = pluginRegistry.getItemRegistrationPlugins().size();
		int postLoadPlugins = pluginRegistry.getPostLoadPlugins().size();
		LOGGER.info("Discovered and registered {} plugins: {} data, {} item registration, {} post-load",
				dataPlugins + itemRegPlugins + postLoadPlugins, dataPlugins, itemRegPlugins, postLoadPlugins);
	}

	private TagResolver loadTagResolver() {
		LOGGER.info("Loading TagResolver from all namespaces...");
		List<String> namespaces = List.of(MOD_NAMESPACE, "minecraft");
		IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace(MOD_NAMESPACE).build();
		TagLoadingService tagLoader = new TagLoadingService(idFactory);

		return namespaces.stream()
				.map(ns -> tagLoader.loadTags(new OpenIdentifier(ns, "tags")))
				.reduce(TagResolver.empty(), TagResolver::merge);
	}


	private PluginRegistrationContextImpl registerPluginRequirements(Supplier<TagResolver> tagResolverSupplier) {
		PluginRegistrationContextImpl registrationContext = new PluginRegistrationContextImpl(tagResolverSupplier);
		for (DataPlugin plugin : pluginRegistry.getDataPlugins()) {
			try {
				LOGGER.debug("Registering requirements for data plugin: {}", plugin.getId());
				plugin.register(registrationContext);
			} catch (Exception e) {
				LOGGER.error("Failed to register data plugin: {}", plugin.getId(), e);
			}
		}
		return registrationContext;
	}

	private ForgeroDataInitializer.Config createDataConfig(PluginRegistrationContextImpl registrationContext, TagResolver tagResolver) {
		Map<String, Codec<? extends StaticCondition>> staticConditionCodecs = registrationContext.getStaticConditionCodecs();
		Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs = registrationContext.getDynamicConditionCodecs();

		// Lazily create the ConditionCodec so it's only made once and can be shared.
		Supplier<Codec<Condition>> conditionCodecSupplier = () -> new ConditionCodec(staticConditionCodecs, dynamicConditionCodecs);

		// Build the full map of property codecs from plugin-provided builders
		Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs = new HashMap<>();

		// Add Forgero's default attribute codec
		propertyCodecs.put(Attribute.KEY, ListCodecWrapper.of(new AttributeCodec(conditionCodecSupplier.get())));

		// Add all codecs from plugins
		var propertyCodecBuilders = registrationContext.getPropertyCodecBuilders();
		LOGGER.debug("Building {} property codecs from plugins.", propertyCodecBuilders.size());
		for (var entry : propertyCodecBuilders.entrySet()) {
			propertyCodecs.put(entry.getKey(), entry.getValue().apply(conditionCodecSupplier));
		}

		return new ForgeroDataInitializer.Config(
				MOD_NAMESPACE,
				new ClassPathResourceProvider("data"),
				tagResolver,
				propertyCodecs,
				staticConditionCodecs,
				dynamicConditionCodecs
		);
	}

	private void initializeRecipes(ForgeroDataInitializer.Config dataConfig) {
		// Instantiate and register the serializer
		ForgeroShapedRecipeSerializer.INSTANCE = new ForgeroShapedRecipeSerializer(dataConfig.propertyCodecs());
		Registry.register(Registries.RECIPE_SERIALIZER, ForgeroShapedRecipeSerializer.ID, ForgeroShapedRecipeSerializer.INSTANCE);
		LOGGER.info("Registered Forgero shaped recipe serializer.");
	}

	private ForgeroDataBundle loadData(ForgeroDataInitializer.Config config) {
		LOGGER.info("Loading Forgero data bundle...");
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(config);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		LOGGER.info("Data bundle loaded with {} components", bundle.componentRegistry().all().size());
		return bundle;
	}

	private void initializeCoreServices(ForgeroDataBundle bundle, PluginRegistrationContextImpl registrationContext, ForgeroDataInitializer.Config dataConfig, DynamicItems dynamicItems) {
		LOGGER.debug("Initializing core services...");

		ComponentRegistry componentRegistry = new MapBackedComponentRegistry(
				bundle.componentRegistry().all().stream()
						.collect(Collectors.toMap(Component::id, Function.identity()))
		);

		ComponentConstructor constructorRegistry = ComponentConstructor.defaults();

		Codec<Component> componentCodec = createComponentCodec(componentRegistry, constructorRegistry, new KeyMapDispatchCodec(dataConfig.propertyCodecs()).codec());

		ComponentNbtConverter nbtConverter = new ComponentNbtConverter(componentCodec);

		IdMapper idMapper = new IdMapper(bundle.hostItemMap());
		TypeConverter typeConverter = new TypeConverter(idMapper, componentRegistry, dynamicItems.item(), dynamicItems.tool(), dynamicItems.sword());
		StatefulConverter statefulConverter = new StatefulConverter(nbtConverter, typeConverter);
		ComponentConverter componentConverter = new ComponentConverterImpl(statefulConverter, typeConverter, idMapper, componentRegistry);

		Resolver resolver = new ResolverEngine();

		context.initialize(
				componentRegistry,
				bundle.componentRegistry(),
				resolver,
				componentConverter,
				nbtConverter,
				bundle
		);

		AttributeManager.initialize(componentConverter, resolver);
		LOGGER.debug("Forgero Attribute Manager initialized.");

		// Initialize the environment for recipes
		ComponentMutater mutater = new ComponentMutaterImpl();
		ForgeroEnvironment.initialize(componentRegistry, componentConverter, mutater);
		LOGGER.debug("Forgero Environment initialized for crafting.");


		LOGGER.debug("Core services initialized");
	}


	private Codec<Component> createComponentCodec(ComponentRegistry componentRegistry, ComponentConstructor constructorRegistry, Codec<Map<String, List<?>>> propertyMapCodec) {
		Codec<CofComponent> cofComponentCodec = CofCodecs.create(propertyMapCodec);

		return new ComponentCofCodec(
				componentRegistry,
				constructorRegistry,
				cofComponentCodec
		);
	}


	private ItemRegistrar setupItemRegistration() {
		ItemRegistrar registrar = new ItemRegistrar(
				context.getComponentRegistry(),
				context.getResolver(),
				LOGGER
		);

		ItemRegistrationCallbackContextImpl callbackContext = new ItemRegistrationCallbackContextImpl(registrar);
		for (ItemRegistrationPlugin plugin : pluginRegistry.getItemRegistrationPlugins()) {
			try {
				LOGGER.debug("Registering item callbacks from plugin: {}", plugin.getId());
				plugin.registerCallbacks(callbackContext);
			} catch (Exception e) {
				LOGGER.error("Failed to register item callbacks from plugin: {}", plugin.getId(), e);
			}
		}
		return registrar;
	}

	private List<ItemRegistrar.RegisteredItem> processItems(
			ForgeroDataBundle bundle,
			Map<String, ItemCreator> creators,
			ItemRegistrar registrar) {
		LOGGER.info("Processing items with {} creators", creators.size());
		return registrar.registerItems(bundle.hostItemMap(), creators);
	}

	private void notifyPostLoadPlugins() {
		for (PostLoadPlugin plugin : pluginRegistry.getPostLoadPlugins()) {
			try {
				LOGGER.debug("Notifying post-load plugin: {}", plugin.getId());
				plugin.onDataLoaded(context);
			} catch (Exception e) {
				LOGGER.error("Error in post-load plugin: {}", plugin.getId(), e);
			}
		}
	}

	public Map<Identifier, Item> getRegisteredItems() {
		return Collections.unmodifiableMap(registeredItems);
	}

	/**
	 * @deprecated Use {@link ForgeroApi#converter()} or other methods on {@link ForgeroApi} instead. This method will be removed in a future version.
	 */
	@Deprecated(forRemoval = true)
	public static DataLoadingContext getContext() {
		if (INSTANCE == null || !INSTANCE.initialized) {
			throw new IllegalStateException("ForgeroDataLoader not initialized");
		}
		return INSTANCE.context;
	}

	/**
	 * @deprecated This method is for internal use and will be removed.
	 */
	@Deprecated(forRemoval = true)
	public static Map<Identifier, Item> getItems() {
		if (INSTANCE == null || !INSTANCE.initialized) {
			throw new IllegalStateException("ForgeroDataLoader not initialized");
		}
		return INSTANCE.getRegisteredItems();
	}

	private record ItemRegistrationCallbackContextImpl(
			ItemRegistrar registrar) implements ItemRegistrationCallbackContext {

		@Override
		public void addCallback(ItemRegistrationCallback callback) {
			registrar.addRegistrationCallback(callback);
		}
	}
}
