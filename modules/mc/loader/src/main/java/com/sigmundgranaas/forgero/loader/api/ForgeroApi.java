package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Convenience API for external mod developers to access Forgero's core systems.
 * <p>
 * This class provides static methods that wrap the {@link ForgeroServices} interface,
 * offering a simple entry point for mods that prefer static access over dependency injection.
 *
 * <h2>For External Mod Developers</h2>
 * <p>
 * This API is designed for simplicity. Just call the static methods:
 * <pre>{@code
 * // Convert an ItemStack to a Component
 * Optional<Component> tool = ForgeroApi.component(player.getMainHandStack());
 *
 * // Query tag relationships
 * TagResolver resolver = ForgeroApi.tagResolver();
 * boolean isMetal = resolver.hasTag(component, metalTag);
 *
 * // Compute attributes
 * AttributeQueryResult result = ForgeroApi.resolver().resolve(component, new AttributeEngine());
 * float damage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
 * }</pre>
 *
 * <h2>For Internal Code / Advanced Usage</h2>
 * <p>
 * For better testability and cleaner architecture, prefer using {@link ForgeroServices}
 * with dependency injection via the {@link ForgeroInitializedCallback} event:
 * <pre>{@code
 * ForgeroInitializedCallback.EVENT.register(services -> {
 *     // Store services reference for later use
 *     this.forgeroServices = services;
 * });
 * }</pre>
 *
 * @see ForgeroServices for the DI-friendly service interface
 * @see ForgeroInitializedCallback for event-based initialization
 */
public final class ForgeroApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroApi.class);
	private static ForgeroServices SERVICES;

	private ForgeroApi() {
	}

	/**
	 * Initializes the Forgero API. This method is for internal use by the Forgero data loader only.
	 *
	 * @param services The fully loaded services container to back the API.
	 */
	public static void initialize(ForgeroServices services) {
		if (SERVICES != null) {
			LOGGER.warn("ForgeroApi is being initialized more than once. This may indicate an issue.");
		}
		SERVICES = services;
	}

	/**
	 * Returns the underlying services instance.
	 * <p>
	 * For most use cases, prefer using the individual static methods like {@link #converter()},
	 * {@link #tagResolver()}, etc. This method is provided for cases where you need to pass
	 * the services container to another component.
	 *
	 * @return The ForgeroServices instance
	 * @throws IllegalStateException if Forgero has not been initialized
	 */
	public static ForgeroServices services() {
		ensureInitialized();
		return SERVICES;
	}

	private static void ensureInitialized() {
		if (SERVICES == null) {
			throw new IllegalStateException(
					"Forgero API has not been initialized. " +
					"Please ensure Forgero has loaded correctly before accessing the API. " +
					"For reliable access, use ForgeroInitializedCallback.EVENT to receive services when ready."
			);
		}
	}

	// ============================================================
	// Convenience static methods (delegate to ForgeroServices)
	// ============================================================

	/**
	 * Converts an ItemStack to its corresponding Component.
	 *
	 * @param stack The ItemStack to convert
	 * @return The component if the stack represents a Forgero item, empty otherwise
	 */
	public static Optional<Component> component(ItemStack stack) {
		ensureInitialized();
		return SERVICES.component(stack);
	}

	/**
	 * Returns the primary converter for all Forgero/Minecraft conversions.
	 *
	 * @return The ComponentConverter instance
	 */
	public static ComponentConverter converter() {
		ensureInitialized();
		return SERVICES.converter();
	}

	/**
	 * Returns the tag resolver for querying tag relationships and inheritance.
	 *
	 * @return The TagResolver instance
	 */
	public static TagResolver tagResolver() {
		ensureInitialized();
		return SERVICES.tagResolver();
	}

	/**
	 * Returns the property resolver for computing attributes and features.
	 *
	 * @return The Resolver instance
	 */
	public static Resolver resolver() {
		ensureInitialized();
		return SERVICES.resolver();
	}

	/**
	 * Returns the tagged registry for tag-based component queries.
	 *
	 * @return The TaggedRegistry for Components
	 */
	public static TaggedRegistry<Component> taggedComponents() {
		ensureInitialized();
		return SERVICES.taggedComponents();
	}

	/**
	 * Returns the component registry containing all loaded default-state components.
	 *
	 * @return The ComponentRegistry instance
	 */
	public static ComponentRegistry componentRegistry() {
		ensureInitialized();
		return SERVICES.componentRegistry();
	}

	/**
	 * Returns the NBT converter for serializing and deserializing components.
	 *
	 * @return The ComponentNbtConverter instance
	 */
	public static ComponentNbtConverter nbtConverter() {
		ensureInitialized();
		return SERVICES.nbtConverter();
	}

	// ============================================================
	// Deprecated methods (for backward compatibility)
	// ============================================================

	/**
	 * @deprecated Use {@link #taggedComponents()} instead
	 */
	@Deprecated(forRemoval = true)
	public static TaggedRegistry<Component> components() {
		return taggedComponents();
	}

	/**
	 * @deprecated Use {@link #componentRegistry()} instead
	 */
	@Deprecated(forRemoval = true)
	public static ComponentRegistry defaultComponents() {
		return componentRegistry();
	}
}
