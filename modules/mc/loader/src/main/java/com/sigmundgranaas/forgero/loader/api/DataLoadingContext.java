package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/**
 * Context object providing access to all loaded data and registries.
 * <p>
 * This interface extends {@link ForgeroServices} to provide both the clean DI-friendly
 * API and legacy getter methods for backward compatibility.
 *
 * @see ForgeroServices for the primary service interface
 */
public interface DataLoadingContext extends ForgeroServices {

	// ============================================================
	// ForgeroServices implementation (preferred API)
	// ============================================================

	@Override
	default TagResolver tagResolver() {
		return getDataBundle().tagResolver();
	}

	@Override
	default ComponentConverter converter() {
		return getConverter();
	}

	@Override
	default Resolver resolver() {
		return getResolver();
	}

	@Override
	default ComponentRegistry componentRegistry() {
		return getComponentRegistry();
	}

	@Override
	default TaggedRegistry<Component> taggedComponents() {
		return getTaggedComponentRegistry();
	}

	@Override
	default ComponentNbtConverter nbtConverter() {
		return getNbtConverter();
	}

	@Override
	default Optional<Component> component(ItemStack stack) {
		return getComponent(stack);
	}

	@Override
	default com.sigmundgranaas.forgero.core.component.api.slot.SlotManager slotManager() {
		return getSlotManager();
	}

	// ============================================================
	// Legacy API (for backward compatibility)
	// ============================================================

	/**
	 * @deprecated Use {@link #componentRegistry()} instead
	 */
	@Deprecated(forRemoval = true)
	ComponentRegistry getComponentRegistry();

	/**
	 * @deprecated Use {@link #taggedComponents()} instead
	 */
	@Deprecated(forRemoval = true)
	TaggedRegistry<Component> getTaggedComponentRegistry();

	/**
	 * @deprecated Use {@link #resolver()} instead
	 */
	@Deprecated(forRemoval = true)
	Resolver getResolver();

	/**
	 * @deprecated Use {@link #component(ItemStack)} instead
	 */
	@Deprecated(forRemoval = true)
	Optional<Component> getComponent(ItemStack stack);

	/**
	 * @deprecated Use {@link #converter()} instead
	 */
	@Deprecated(forRemoval = true)
	ComponentConverter getConverter();

	/**
	 * @deprecated Use {@link #nbtConverter()} instead
	 */
	@Deprecated(forRemoval = true)
	ComponentNbtConverter getNbtConverter();

	/**
	 * @deprecated Use {@link #slotManager()} instead
	 */
	@Deprecated(forRemoval = true)
	com.sigmundgranaas.forgero.core.component.api.slot.SlotManager getSlotManager();

	/**
	 * Get the original data bundle for modules that need raw data access.
	 * <p>
	 * Note: Prefer using specific service methods rather than accessing the bundle directly.
	 */
	ForgeroDataBundle getDataBundle();
}
