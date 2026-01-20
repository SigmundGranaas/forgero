package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
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
	// ForgeroServices methods (preferred API) - implemented by DataLoadingContextImpl
	// ============================================================

	@Override
	TagResolver tagResolver();

	@Override
	ComponentConverter converter();

	@Override
	ComponentRegistry componentRegistry();

	@Override
	TaggedRegistry<Component> taggedComponents();

	@Override
	ComponentNbtConverter nbtConverter();

	@Override
	Optional<Component> component(ItemStack stack);

	@Override
	SlotManager slotManager();

	// ============================================================
	// Legacy API (deprecated - delegates to new methods)
	// ============================================================

	/**
	 * @deprecated Use {@link #componentRegistry()} instead
	 */
	@Deprecated(forRemoval = true)
	default ComponentRegistry getComponentRegistry() {
		return componentRegistry();
	}

	/**
	 * @deprecated Use {@link #taggedComponents()} instead
	 */
	@Deprecated(forRemoval = true)
	default TaggedRegistry<Component> getTaggedComponentRegistry() {
		return taggedComponents();
	}

	/**
	 * @deprecated Use {@link #component(ItemStack)} instead
	 */
	@Deprecated(forRemoval = true)
	default Optional<Component> getComponent(ItemStack stack) {
		return component(stack);
	}

	/**
	 * @deprecated Use {@link #converter()} instead
	 */
	@Deprecated(forRemoval = true)
	default ComponentConverter getConverter() {
		return converter();
	}

	/**
	 * @deprecated Use {@link #nbtConverter()} instead
	 */
	@Deprecated(forRemoval = true)
	default ComponentNbtConverter getNbtConverter() {
		return nbtConverter();
	}

	/**
	 * @deprecated Use {@link #slotManager()} instead
	 */
	@Deprecated(forRemoval = true)
	default SlotManager getSlotManager() {
		return slotManager();
	}

	/**
	 * Get the original data bundle for modules that need raw data access.
	 * <p>
	 * Note: Prefer using specific service methods rather than accessing the bundle directly.
	 */
	ForgeroDataBundle getDataBundle();
}
