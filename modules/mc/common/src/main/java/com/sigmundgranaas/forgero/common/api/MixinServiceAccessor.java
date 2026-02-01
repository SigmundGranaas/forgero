// modules/mc/common/src/main/java/com/sigmundgranaas/forgero/common/api/MixinServiceAccessor.java

package com.sigmundgranaas.forgero.common.api;

import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Static accessor for Forgero services from Mixins.
 *
 * <p>This class serves as the bridge between Mixins (which cannot use dependency injection)
 * and Forgero's service-oriented architecture. It holds a reference to ForgeroServices
 * once initialized and provides convenient methods for common Mixin operations.
 *
 * <p><b>Key differences from AttributeManager:</b>
 * <ul>
 *   <li>Does not implement business logic - delegates to services</li>
 *   <li>Single field holding the services reference</li>
 *   <li>Returns empty/defaults if services unavailable (fail-safe)</li>
 *   <li>All attribute calculation logic lives in AttributeEngine</li>
 * </ul>
 *
 * <p><b>Lifecycle:</b> Initialized once by ForgeroDataLoader, then used by Mixins.
 */
public final class MixinServiceAccessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MixinServiceAccessor.class);

	// Single reference to all services - no duplication of state
	private static final AtomicReference<ForgeroServices> SERVICES = new AtomicReference<>();
	private static volatile boolean initialized = false;

	private MixinServiceAccessor() {
		// Prevent instantiation
	}

	/**
	 * Initializes the accessor with the full services container.
	 * Called exactly once during Forgero initialization.
	 */
	public static void initialize(ForgeroServices services) {
		if (services == null) {
			throw new IllegalArgumentException("Services cannot be null");
		}

		if (!SERVICES.compareAndSet(null, services)) {
			LOGGER.warn("MixinServiceAccessor already initialized - ignoring duplicate initialization");
			return;
		}

		initialized = true;
		LOGGER.debug("MixinServiceAccessor initialized");
	}

	/**
	 * Returns the services if initialized, empty otherwise.
	 * Mixins call this to access services lazily.
	 */
	public static Optional<ForgeroServices> getServices() {
		return Optional.ofNullable(SERVICES.get());
	}

	/**
	 * Checks if services are available.
	 * Useful for Mixins to check before attempting operations.
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	// ========================================================================
	// Convenience methods for common Mixin operations
	// These delegate to services, no business logic here
	// ========================================================================

	/**
	 * Gets attribute modifiers for an ItemStack.
	 * Used by ItemStackAttributeMixin.
	 */
	public static Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
			ItemStack stack,
			Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap,
			EquipmentSlot slot
	) {
		ForgeroServices services = SERVICES.get();
		if (services == null || stack.isEmpty()) {
			return vanillaMap;
		}

		return services.itemQuery().getAttributeModifiers(stack, vanillaMap, slot);
	}

	/**
	 * Gets resolved attributes for an ItemStack.
	 * Used by ItemMiningMixin and ItemStackDurabilityMixin.
	 */
	public static Optional<AttributeQueryResult> getResolvedAttributes(ItemStack stack) {
		ForgeroServices services = SERVICES.get();
		if (services == null || stack.isEmpty()) {
			return Optional.empty();
		}

		return services.converter().toComponent(stack)
				.map(component -> new AttributeEngine().resolve(component));
	}

	/**
	 * Gets the Component for an ItemStack if it represents a Forgero item.
	 */
	public static Optional<com.sigmundgranaas.forgero.core.component.api.Component> getComponent(ItemStack stack) {
		ForgeroServices services = SERVICES.get();
		if (services == null || stack.isEmpty()) {
			return Optional.empty();
		}

		return services.converter().toComponent(stack);
	}

	/**
	 * Checks if an ItemStack represents a Forgero item.
	 */
	public static boolean isForgeroItem(ItemStack stack) {
		ForgeroServices services = SERVICES.get();
		if (services == null || stack.isEmpty()) {
			return false;
		}

		return services.itemQuery().isForgeroItem(stack);
	}

	/**
	 * Resets the accessor. Only for use in tests.
	 */
	public static void reset() {
		SERVICES.set(null);
		initialized = false;
	}
}
