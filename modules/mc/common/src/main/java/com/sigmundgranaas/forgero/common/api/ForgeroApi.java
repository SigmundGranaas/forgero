package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;

/**
 * Static entry point for accessing Forgero services.
 * <p>
 * This class provides a convenient static accessor for {@link ForgeroServices} after
 * Forgero has been initialized. For most use cases, prefer using
 * {@link ForgeroInitializedCallback} for proper dependency injection.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * ForgeroServices services = ForgeroApi.services();
 * services.converter().toComponent(stack).ifPresent(component -> {
 *     // use component
 * });
 * }</pre>
 *
 * <h2>Initialization Requirement</h2>
 * This class is only usable after Forgero has been initialized. Calling {@link #services()}
 * before initialization will throw an {@link IllegalStateException}.
 *
 * @see ForgeroServices
 * @see ForgeroInitializedCallback
 */
public final class ForgeroApi {

	private static volatile ForgeroServices servicesInstance;

	private ForgeroApi() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Returns the ForgeroServices instance.
	 *
	 * @return The services instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ForgeroServices services() {
		ForgeroServices services = servicesInstance;
		if (services == null) {
			throw new IllegalStateException(
				"ForgeroApi.services() called before initialization. " +
				"Use ForgeroInitializedCallback.EVENT to receive services when ready."
			);
		}
		return services;
	}

	/**
	 * Checks if Forgero has been initialized and services are available.
	 *
	 * @return true if services are available, false otherwise
	 */
	public static boolean isInitialized() {
		return servicesInstance != null;
	}

	// Convenience delegate methods

	/**
	 * Returns the tag resolver for querying tag relationships.
	 * Convenience method equivalent to {@code services().tagResolver()}.
	 *
	 * @return The tag resolver instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static TagResolver tagResolver() {
		return services().tagResolver();
	}

	/**
	 * Returns the component converter for Forgero/Minecraft conversions.
	 * Convenience method equivalent to {@code services().converter()}.
	 *
	 * @return The component converter instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ComponentConverter converter() {
		return services().converter();
	}

	/**
	 * Returns the component registry containing all loaded components.
	 * Convenience method equivalent to {@code services().componentRegistry()}.
	 *
	 * @return The component registry instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ComponentRegistry componentRegistry() {
		return services().componentRegistry();
	}

	/**
	 * Returns the tagged registry for tag-based component queries.
	 * Convenience method equivalent to {@code services().taggedComponents()}.
	 *
	 * @return The tagged component registry
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static TaggedRegistry<Component> taggedComponents() {
		return services().taggedComponents();
	}

	/**
	 * Returns the NBT converter for serializing components.
	 * Convenience method equivalent to {@code services().nbtConverter()}.
	 *
	 * @return The NBT converter instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ComponentNbtConverter nbtConverter() {
		return services().nbtConverter();
	}

	/**
	 * Returns the slot manager for slot operations.
	 * Convenience method equivalent to {@code services().slotManager()}.
	 *
	 * @return The slot manager instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static SlotManager slotManager() {
		return services().slotManager();
	}

	/**
	 * Returns the read-only ItemStack query API.
	 * Convenience method equivalent to {@code services().itemQuery()}.
	 *
	 * @return The ItemStack query API instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ItemQueryApi itemQuery() {
		return services().itemQuery();
	}

	/**
	 * Returns the ItemStack mutation API.
	 * Convenience method equivalent to {@code services().itemMutation()}.
	 *
	 * @return The ItemStack mutation API instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ItemMutationApi itemMutation() {
		return services().itemMutation();
	}

	/**
	 * Returns the ItemStack comparison API.
	 * Convenience method equivalent to {@code services().itemComparison()}.
	 *
	 * @return The ItemStack comparison API instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ItemComparisonApi itemComparison() {
		return services().itemComparison();
	}

	/**
	 * Returns the ItemStack property resolution API.
	 * Convenience method equivalent to {@code services().itemProperty()}.
	 *
	 * @return The ItemStack property API instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static ItemPropertyApi itemProperty() {
		return services().itemProperty();
	}

	/**
	 * Internal initialization API.
	 * <p>
	 * <b>INTERNAL USE ONLY:</b> This class is intended for use by the Forgero loader
	 * infrastructure only. Do not call these methods from external code.
	 */
	public static final class Initializer {

		private Initializer() {
			// Private constructor to prevent instantiation
		}

		/**
		 * Initializes the API with the provided services and fires the initialization event.
		 *
		 * @param services The services instance to set
		 * @throws IllegalArgumentException if services is null
		 * @throws IllegalStateException if services have already been initialized
		 */
		public static void initialize(ForgeroServices services) {
			if (services == null) {
				throw new IllegalArgumentException("services cannot be null");
			}
			if (servicesInstance != null) {
				throw new IllegalStateException(
					"ForgeroApi has already been initialized. Cannot reinitialize with new services."
				);
			}
			servicesInstance = services;
			ForgeroInitializedCallback.EVENT.invoker().onForgeroInitialized(services);
		}

		/**
		 * Resets the API state. Only for use in tests.
		 */
		public static void reset() {
			servicesInstance = null;
		}
	}
}
