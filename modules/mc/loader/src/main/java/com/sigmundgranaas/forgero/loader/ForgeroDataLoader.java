package com.sigmundgranaas.forgero.loader;

import com.sigmundgranaas.forgero.common.attribute.AttributeManager;
import com.sigmundgranaas.forgero.common.item.DynamicItem;
import com.sigmundgranaas.forgero.common.item.DynamicSwordItem;
import com.sigmundgranaas.forgero.common.item.DynamicToolItem;
import com.sigmundgranaas.forgero.common.recipe.ForgeroShapedRecipeSerializer;
import com.sigmundgranaas.forgero.common.recipe.RecipeServices;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tooltip.ForgeroTooltipRenderer;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.component.api.slot.impl.SlotManagerImpl;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.impl.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central data loader that orchestrates the entire Forgero data loading process.
 * Delegates to focused classes for specific concerns:
 * - {@link PluginOrchestrator} for plugin lifecycle management
 * - {@link DataLoadingPipeline} for tag and data loading
 * - {@link ComponentRegistrationService} for core services initialization
 * - {@link ApiInitializer} for event firing and API exposure
 */
public class ForgeroDataLoader implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataLoader.class);
	private static final String MOD_NAMESPACE = "forgero";

	private static ForgeroDataLoader INSTANCE;

	// Focused orchestrators
	private final PluginOrchestrator plugins = new PluginOrchestrator();
	private final DataLoadingPipeline dataLoader = new DataLoadingPipeline();
	private final ComponentRegistrationService componentService = new ComponentRegistrationService();
	private final ApiInitializer apiInitializer = new ApiInitializer();

	// Shared services
	private ComponentMutater componentMutater;

	// State
	private final DataLoadingContextImpl context = new DataLoadingContextImpl();
	private final Map<Identifier, Item> registeredItems = new HashMap<>();
	private boolean initialized = false;
	private TagResolver tagResolver = TagResolver.empty();

	public ForgeroDataLoader() {
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
			ComponentRegistrationService.DynamicItems dynamicItems = registerDynamicItems();

			// Phase 1: Discover plugins
			plugins.discoverPlugins();

			// Phase 2: Load tags
			this.tagResolver = dataLoader.loadTags();

			// Phase 3: Register plugin requirements
			PluginRegistrationContextImpl registrationContext =
					plugins.registerPluginRequirements(() -> this.tagResolver);

			// Phase 3.5: Register slot codecs into SlotRegistry
			registrationContext.getSlotCodecs().forEach((type, codec) -> {
				com.sigmundgranaas.forgero.core.component.api.slot.SlotRegistry.register(type, codec);
				LOGGER.debug("Registered slot codec for type: {}", type);
			});

			// Phase 4: Create data configuration
			ForgeroDataInitializer.Config dataConfig =
					dataLoader.createConfig(registrationContext, this.tagResolver);

			// Phase 5: Load data
			ForgeroDataBundle bundle = dataLoader.loadData(dataConfig);

			// Phase 6: Initialize core services
			ComponentRegistrationService.ServiceBundle services =
					componentService.initializeServices(bundle, dataConfig, dynamicItems);

			// Initialize context with services
			initializeContext(bundle, services);

			// Initialize legacy services (AttributeManager for mixin access)
			initializeLegacyServices(services);

			// Phase 7: Setup item registration callbacks
			ItemRegistrar itemRegistrar = new ItemRegistrar(
					context.componentRegistry(),
					context.resolver(),
					LOGGER
			);
			plugins.setupItemCallbacks(itemRegistrar);

			// Phase 8: Process and register items
			List<ItemRegistrar.RegisteredItem> items =
					itemRegistrar.registerItems(bundle.hostItemMap(), registrationContext.getItemCreators());
			items.forEach(item -> registeredItems.put(item.id(), item.item()));

			// Phase 9: Notify post-load plugins
			plugins.notifyPostLoad(context);

			// Phase 10: Initialize recipe serializers
			initializeRecipes(dataConfig);

			initialized = true;
			ForgeroTooltipRenderer.initialize(context.converter(), context.resolver());

			// Phase 11: Fire initialization event (external subscribers via ForgeroInitializedCallback)
			apiInitializer.fireInitializationEvent(context);
			LOGGER.info("Forgero initialization complete. Services available via ForgeroInitializedCallback.");

			long endTime = System.currentTimeMillis();
			LOGGER.info("Forgero data loading complete. Registered {} items in {}ms",
					registeredItems.size(), endTime - startTime);

		} catch (Exception e) {
			LOGGER.error("Critical error during Forgero data loading", e);
			throw new RuntimeException("Failed to initialize Forgero", e);
		}
	}

	private ComponentRegistrationService.DynamicItems registerDynamicItems() {
		Item.Settings settings = new Item.Settings();
		Item dynamicItem = Registry.register(
				Registries.ITEM,
				new Identifier(MOD_NAMESPACE, "dynamic_item"),
				new DynamicItem(settings)
		);
		Item dynamicToolItem = Registry.register(
				Registries.ITEM,
				new Identifier(MOD_NAMESPACE, "dynamic_tool"),
				new DynamicToolItem(settings)
		);
		Item dynamicSwordItem = Registry.register(
				Registries.ITEM,
				new Identifier(MOD_NAMESPACE, "dynamic_sword"),
				new DynamicSwordItem(settings)
		);

		LOGGER.info("Registered dynamic items for NBT-driven component wrapping.");
		return new ComponentRegistrationService.DynamicItems(dynamicItem, dynamicToolItem, dynamicSwordItem);
	}

	private void initializeContext(
			ForgeroDataBundle bundle,
			ComponentRegistrationService.ServiceBundle services
	) {
		// Create ComponentMutater and SlotManager for services
		ComponentMutater mutater = new ComponentMutaterImpl();
		SlotManager slotManager = new SlotManagerImpl(mutater);

		context.initialize(
				services.componentRegistry(),
				bundle.componentRegistry(),
				services.resolver(),
				services.converter(),
				services.nbtConverter(),
				slotManager,
				bundle
		);

		// Store mutater for legacy services initialization
		this.componentMutater = mutater;
	}

	private void initializeLegacyServices(ComponentRegistrationService.ServiceBundle services) {
		// Initialize AttributeManager for mixin access
		AttributeManager.initialize(services.converter(), services.resolver());
		LOGGER.debug("Forgero Attribute Manager initialized.");
	}

	private void initializeRecipes(ForgeroDataInitializer.Config dataConfig) {
		RecipeServices recipeServices = new RecipeServices(
				context.componentRegistry(),
				context.converter(),
				componentMutater
		);

		ForgeroShapedRecipeSerializer.INSTANCE =
				new ForgeroShapedRecipeSerializer(dataConfig.propertyCodecs(), recipeServices);
		Registry.register(
				Registries.RECIPE_SERIALIZER,
				ForgeroShapedRecipeSerializer.ID,
				ForgeroShapedRecipeSerializer.INSTANCE
		);
		LOGGER.info("Registered Forgero shaped recipe serializer.");
	}

	public Map<Identifier, Item> getRegisteredItems() {
		return Collections.unmodifiableMap(registeredItems);
	}

	/**
	 * @deprecated Use {@link ForgeroInitializedCallback} to receive services instead.
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
}
