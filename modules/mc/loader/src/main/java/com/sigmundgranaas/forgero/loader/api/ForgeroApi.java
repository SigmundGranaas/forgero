package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The official public API for Forgero.
 * This class provides a stable, static entry point for external developers to access Forgero's core systems,
 * such as component registries, the tag graph, the property resolver, and conversion utilities.
 * <p>
 * This API is guaranteed to be available after Forgero's main initialization phase is complete.
 *
 * Example usage:
 * <pre>{@code
 * Optional<Component> forgeroTool = ForgeroApi.component(player.getMainHandStack());
 * forgeroTool.ifPresent(tool -> {
 *     float attackDamage = ForgeroApi.resolver().resolve(tool, new AttributeEngine()).getValue(DefaultAttributes.ATTACK_DAMAGE);
 *     System.out.println("Forgero Tool Attack Damage: " + attackDamage);
 * });
 * }</pre>
 */
public final class ForgeroApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroApi.class);
	private static DataLoadingContext CONTEXT;

	private ForgeroApi() {
	}

	/**
	 * Initializes the Forgero API. This method is for internal use by the Forgero data loader only.
	 *
	 * @param context The fully loaded data context to back the API.
	 */
	public static void initialize(DataLoadingContext context) {
		if (CONTEXT != null) {
			LOGGER.warn("ForgeroApi is being initialized more than once. This may indicate an issue.");
		}
		CONTEXT = context;
	}

	private static void ensureInitialized() {
		if (CONTEXT == null) {
			throw new IllegalStateException("Forgero API has not been initialized. Please ensure Forgero has loaded correctly before accessing the API.");
		}
	}

	/**
	 * Provides the primary converter for all Forgero/Minecraft conversions.
	 * This is the recommended way to convert between {@link Component} and {@link ItemStack}.
	 *
	 * @return The singleton instance of the ComponentConverter.
	 */
	public static ComponentConverter converter() {
		ensureInitialized();
		return CONTEXT.getConverter();
	}

	/**
	 * Provides the loaded tag graph
	 *
	 * @return The singleton instance of the TagGraph.
	 */
	public static TagGraph tagGraph() {
		ensureInitialized();
		return CONTEXT.getDataBundle().tagGraph();
	}

	/**
	 * Provides the property resolver for computing attributes and features.
	 *
	 * @return The singleton instance of the Resolver.
	 */
	public static Resolver resolver() {
		ensureInitialized();
		return CONTEXT.getResolver();
	}

	/**
	 * Provides the tagged registry for all loaded components, allowing for powerful tag-based queries.
	 *
	 * @return The singleton instance of the TaggedRegistry for Components.
	 */
	public static TaggedRegistry<Component> components() {
		ensureInitialized();
		return CONTEXT.getTaggedComponentRegistry();
	}

	/**
	 * Provides the component registry containing all loaded default-state components.
	 *
	 * @return The singleton instance of the ComponentRegistry.
	 */
	public static ComponentRegistry defaultComponents() {
		ensureInitialized();
		return CONTEXT.getComponentRegistry();
	}
}
