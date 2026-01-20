package com.sigmundgranaas.forgero.loader.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeCodec;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.loader.impl.phase.PhaseExecutor;
import com.sigmundgranaas.forgero.loader.impl.phase.PhaseResult;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CachingResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles the data loading pipeline phases.
 * Responsible for phases 2, 4, and 5 of the initialization.
 * <p>
 * Supports graceful degradation via {@link PhaseResult} - if a phase fails,
 * fallback values are used and initialization continues.
 */
public class DataLoadingPipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataLoadingPipeline.class);
	private final PhaseExecutor executor = new PhaseExecutor("DataLoadingPipeline");
	private static final String MOD_NAMESPACE = "forgero";

	// Shared cached provider - created once, reused across all phases
	private final CachingResourceProvider sharedProvider;

	/**
	 * Creates a new DataLoadingPipeline with a shared cached resource provider.
	 * <p>
	 * The provider is created once and reused across all loading phases (tags, data, etc.)
	 * to avoid redundant mod discovery and filesystem traversal.
	 */
	public DataLoadingPipeline() {
		boolean devMode = Boolean.getBoolean("forgero.dev.hotReload");
		FabricResourceProvider fabricProvider = new FabricResourceProvider("data");
		this.sharedProvider = new CachingResourceProvider(fabricProvider, devMode);
		LOGGER.debug("DataLoadingPipeline initialized (devMode={})", devMode);
	}

	/**
	 * Phase 2: Load the TagResolver from all namespaces.
	 *
	 * @return The merged TagResolver from all namespaces
	 */
	public TagResolver loadTags() {
		return loadTagsSafe().value();
	}

	/**
	 * Phase 2: Load the TagResolver from all namespaces with graceful degradation.
	 *
	 * @return A PhaseResult containing the TagResolver or an empty fallback
	 */
	public PhaseResult<TagResolver> loadTagsSafe() {
		return executor.execute("loadTags", () -> {
			List<String> namespaces = List.of(MOD_NAMESPACE, "minecraft");
			IdentifierFactory idFactory = new IdentifierFactory.Builder()
					.defaultNamespace(MOD_NAMESPACE)
					.build();

			// Use shared cached provider instead of creating a new one
			TagLoadingService tagLoader = new TagLoadingService(idFactory, sharedProvider);

			return namespaces.stream()
					.map(ns -> tagLoader.loadTags(new OpenIdentifier(ns, "tags")))
					.reduce(TagResolver.empty(), TagResolver::merge);
		}, TagResolver.empty());
	}

	/**
	 * Phase 4: Create the configuration for the data initializer.
	 *
	 * @param registrationContext The plugin registration context with codecs
	 * @param tagResolver         The loaded tag resolver
	 * @return Configuration for the data initializer
	 */
	public ForgeroDataInitializer.Config createConfig(
			PluginRegistrationContextImpl registrationContext,
			TagResolver tagResolver
	) {
		Map<String, Codec<? extends StaticCondition>> staticConditionCodecs =
				registrationContext.getStaticConditionCodecs();
		Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs =
				registrationContext.getDynamicConditionCodecs();

		// Lazily create the ConditionCodec so it's only made once and can be shared
		Supplier<Codec<Condition>> conditionCodecSupplier =
				() -> new ConditionCodec(staticConditionCodecs, dynamicConditionCodecs);

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

		// Use shared cached provider instead of creating a new one
		return new ForgeroDataInitializer.Config(
				MOD_NAMESPACE,
				sharedProvider,
				tagResolver,
				propertyCodecs,
				staticConditionCodecs,
				dynamicConditionCodecs
		);
	}

	/**
	 * Phase 5: Load the data bundle using the configuration.
	 *
	 * @param config The data initializer configuration
	 * @return The loaded data bundle
	 */
	public ForgeroDataBundle loadData(ForgeroDataInitializer.Config config) {
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(config);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		LOGGER.debug("Data bundle loaded: {} components", bundle.componentRegistry().all().size());
		return bundle;
	}

	/**
	 * Gets the mod namespace used for data loading.
	 */
	public String getModNamespace() {
		return MOD_NAMESPACE;
	}

	/**
	 * Gets the shared cached resource provider.
	 * Useful for debugging and cache inspection.
	 *
	 * @return The shared CachingResourceProvider
	 */
	public CachingResourceProvider getSharedProvider() {
		return sharedProvider;
	}

	/**
	 * Invalidates all cached resources.
	 * Call this when resources may have changed (e.g., F3+T reload).
	 */
	public void invalidateCache() {
		sharedProvider.invalidateAll();
		LOGGER.debug("Resource cache invalidated");
	}

	/**
	 * Logs cache statistics for debugging.
	 */
	public void logCacheStatistics() {
		var stats = sharedProvider.getStatistics();
		LOGGER.debug("Cache statistics: listCacheSize={}, resourceCacheSize={}, trackedSources={}",
				stats.get("listCacheSize"),
				stats.get("resourceCacheSize"),
				stats.get("trackedSources"));
	}
}
