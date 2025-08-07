package com.sigmundgranaas.forgero.loader;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;
import com.sigmundgranaas.forgero.cof.codec.CofCodecs;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.attribute.AttributeManager;
import com.sigmundgranaas.forgero.common.convert.*;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.core.property.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.core.registry.impl.MapBackedComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.loader.api.*;
import com.sigmundgranaas.forgero.loader.impl.DataLoadingContextImpl;
import com.sigmundgranaas.forgero.loader.impl.ItemRegistrar;
import com.sigmundgranaas.forgero.loader.impl.PluginRegistrationContextImpl;
import com.sigmundgranaas.forgero.loader.impl.PluginRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
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
			// Phase 1: Collect all plugins
			collectPlugins();

			// Phase 2: Register plugin requirements
			PluginRegistrationContextImpl registrationContext = registerPluginRequirements();

			// Phase 3: Load data
			ForgeroDataBundle bundle = loadData();

			// Phase 4: Initialize core systems
			initializeCoreServices(bundle, registrationContext);

			// Phase 5: Setup item registration callbacks
			ItemRegistrar itemRegistrar = setupItemRegistration();

			// Phase 6: Process and register items
			List<ItemRegistrar.RegisteredItem> items = processItems(bundle, registrationContext.getItemCreators(), itemRegistrar);

			// Store registered items for lookup
			items.forEach(item -> registeredItems.put(item.id(), item.item()));

			// Phase 7: Notify post-load plugins
			notifyPostLoadPlugins();

			initialized = true;
			long endTime = System.currentTimeMillis();
			LOGGER.info("Forgero data loading complete. Registered {} items in {}ms",
					registeredItems.size(), endTime - startTime);

		} catch (Exception e) {
			LOGGER.error("Critical error during Forgero data loading", e);
			throw new RuntimeException("Failed to initialize Forgero", e);
		}
	}

	private void collectPlugins() {
		pluginRegistry.discoverPlugins();
		// Logging logic remains the same
	}

	private PluginRegistrationContextImpl registerPluginRequirements() {
		PluginRegistrationContextImpl registrationContext = new PluginRegistrationContextImpl();
		for (DataPlugin plugin : pluginRegistry.getDataPlugins()) {
			try {
				LOGGER.debug("Registering data plugin: {}", plugin.getId());
				plugin.register(registrationContext);
			} catch (Exception e) {
				LOGGER.error("Failed to register data plugin: {}", plugin.getId(), e);
			}
		}
		return registrationContext;
	}

	private ForgeroDataBundle loadData() {
		LOGGER.info("Loading Forgero data bundle...");
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		LOGGER.info("Data bundle loaded with {} components", bundle.componentRegistry().all().size());
		return bundle;
	}

	private void initializeCoreServices(ForgeroDataBundle bundle, PluginRegistrationContextImpl registrationContext) {
		LOGGER.debug("Initializing core services...");

		// Create component registry
		ComponentRegistry componentRegistry = new MapBackedComponentRegistry(
				bundle.componentRegistry().all().stream()
						.collect(Collectors.toMap(Component::id, Function.identity()))
		);

		// Initialize component constructors
		ComponentConstructorRegistry constructorRegistry = ComponentConstructorRegistry.getInstance();
		constructorRegistry.registerCoreTypes();
		// In a full implementation, you'd register plugin constructors here

		// Create the full component codec
		Codec<Component> componentCodec = createComponentCodec(componentRegistry, constructorRegistry, registrationContext);

		// Initialize NBT converter
		ComponentNbtConverter nbtConverter = new ComponentNbtConverter(componentCodec);

		// Initialize all new conversion classes
		IdMapper idMapper = new IdMapper(bundle.hostItemMap());
		TypeConverter typeConverter = new TypeConverter(idMapper, componentRegistry);
		StatefulConverter statefulConverter = new StatefulConverter(nbtConverter, typeConverter);
		ComponentConverter componentConverter = new ComponentConverterImpl(statefulConverter, typeConverter, idMapper, componentRegistry);

		// Create resolver
		Resolver resolver = new ResolverEngine();

		// Store everything in context for other modules to access
		context.initialize(
				componentRegistry,
				bundle.componentRegistry(), // This is the TaggedRegistry
				resolver,
				componentConverter,
				nbtConverter,
				bundle
		);

		// Initialize the attribute manager now that all dependencies are ready.
		AttributeManager.initialize(componentConverter, resolver);
		LOGGER.debug("Forgero Attribute Manager initialized.");

		LOGGER.debug("Core services initialized");
	}

	private Codec<Component> createComponentCodec(ComponentRegistry componentRegistry, ComponentConstructorRegistry constructorRegistry, PluginRegistrationContextImpl registrationContext) {
		Map<String, Codec<? extends StaticCondition>> staticConditionCodecs = new HashMap<>();
		staticConditionCodecs.put("forgero:self_has_tag", TagMatchCondition.CODEC);
		Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs = new HashMap<>();

		// TODO: Register codecs from plugins via registrationContext.getConditionCodecs()

		ConditionCodec conditionCodec = new ConditionCodec(staticConditionCodecs, dynamicConditionCodecs);
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<FeatureData>> featureListCodec = FeatureCodecs.createFeatureDataListCodec();

		Codec<CofComponent> cofComponentCodec = CofCodecs.create(attributeListCodec, featureListCodec);

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

	@Deprecated
	public static DataLoadingContext getContext() {
		if (INSTANCE == null || !INSTANCE.initialized) {
			throw new IllegalStateException("ForgeroDataLoader not initialized");
		}
		return INSTANCE.context;
	}

	@Deprecated
	public static Map<Identifier, Item> getItems() {
		if (INSTANCE == null || !INSTANCE.initialized) {
			throw new IllegalStateException("ForgeroDataLoader not initialized");
		}
		return INSTANCE.getRegisteredItems();
	}

	private static class ItemRegistrationCallbackContextImpl implements ItemRegistrationCallbackContext {
		private final ItemRegistrar registrar;

		ItemRegistrationCallbackContextImpl(ItemRegistrar registrar) {
			this.registrar = registrar;
		}

		@Override
		public void addCallback(ItemRegistrationCallback callback) {
			registrar.addRegistrationCallback(callback);
		}
	}
}
