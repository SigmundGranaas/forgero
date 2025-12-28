package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
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

	/**
	 * Sets the services instance. Called internally during initialization.
	 * <p>
	 * <b>WARNING:</b> This method is intended for internal use only.
	 * Do not call this method from external code.
	 *
	 * @param services The services instance to set
	 */
	public static void setServices(ForgeroServices services) {
		servicesInstance = services;
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
	 * Returns the property resolver for computing attributes.
	 * Convenience method equivalent to {@code services().resolver()}.
	 *
	 * @return The property resolver instance
	 * @throws IllegalStateException if Forgero has not yet been initialized
	 */
	public static Resolver resolver() {
		return services().resolver();
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
}
