package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.api.item.impl.ItemComparisonApiImpl;
import com.sigmundgranaas.forgero.common.api.item.impl.ItemMutationApiImpl;
import com.sigmundgranaas.forgero.common.api.item.impl.ItemPropertyApiImpl;
import com.sigmundgranaas.forgero.common.api.item.impl.ItemQueryApiImpl;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.common.api.DataLoadingContext;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link DataLoadingContext} providing access to all Forgero services.
 * <p>
 * This class implements the new ForgeroServices API methods. The deprecated getter methods
 * in the interface delegate to these implementations via default methods.
 */
public class DataLoadingContextImpl implements DataLoadingContext {
	private ComponentRegistry componentRegistry;
	private TaggedRegistry<Component> taggedComponentRegistry;
	private ComponentConverter componentConverter;
	private ComponentNbtConverter nbtConverter;
	private SlotManager slotManager;
	private ForgeroDataBundle dataBundle;

	// Lazy-initialized ItemStack APIs
	private ItemQueryApi itemQueryApi;
	private ItemMutationApi itemMutationApi;
	private ItemComparisonApi itemComparisonApi;
	private ItemPropertyApi itemPropertyApi;

	/**
	 * Initializes this context with all required services.
	 *
	 * @param componentRegistry       The component registry (must not be null)
	 * @param taggedComponentRegistry The tagged component registry (must not be null)
	 * @param componentConverter      The component converter (must not be null)
	 * @param nbtConverter            The NBT converter (must not be null)
	 * @param slotManager             The slot manager (must not be null)
	 * @param dataBundle              The data bundle (must not be null)
	 * @throws NullPointerException if any parameter is null
	 */
	public void initialize(
			ComponentRegistry componentRegistry,
			TaggedRegistry<Component> taggedComponentRegistry,
			ComponentConverter componentConverter,
			ComponentNbtConverter nbtConverter,
			SlotManager slotManager,
			ForgeroDataBundle dataBundle) {
		this.componentRegistry = Objects.requireNonNull(componentRegistry, "componentRegistry must not be null");
		this.taggedComponentRegistry = Objects.requireNonNull(taggedComponentRegistry, "taggedComponentRegistry must not be null");
		this.componentConverter = Objects.requireNonNull(componentConverter, "componentConverter must not be null");
		this.nbtConverter = Objects.requireNonNull(nbtConverter, "nbtConverter must not be null");
		this.slotManager = Objects.requireNonNull(slotManager, "slotManager must not be null");
		this.dataBundle = Objects.requireNonNull(dataBundle, "dataBundle must not be null");
	}

	// ============================================================
	// ForgeroServices API (new preferred methods)
	// ============================================================

	@Override
	public TagResolver tagResolver() {
		return dataBundle.tagResolver();
	}

	@Override
	public ComponentConverter converter() {
		return componentConverter;
	}

	@Override
	public ComponentRegistry componentRegistry() {
		return componentRegistry;
	}

	@Override
	public TaggedRegistry<Component> taggedComponents() {
		return taggedComponentRegistry;
	}

	@Override
	public ComponentNbtConverter nbtConverter() {
		return nbtConverter;
	}

	@Override
	public Optional<Component> component(ItemStack stack) {
		return componentConverter.toComponent(stack);
	}

	@Override
	public SlotManager slotManager() {
		return slotManager;
	}

	@Override
	public ItemQueryApi itemQuery() {
		if (itemQueryApi == null) {
			itemQueryApi = new ItemQueryApiImpl(componentConverter, slotManager);
		}
		return itemQueryApi;
	}

	@Override
	public ItemMutationApi itemMutation() {
		if (itemMutationApi == null) {
			itemMutationApi = new ItemMutationApiImpl(componentConverter, slotManager);
		}
		return itemMutationApi;
	}

	@Override
	public ItemComparisonApi itemComparison() {
		if (itemComparisonApi == null) {
			itemComparisonApi = new ItemComparisonApiImpl(componentConverter);
		}
		return itemComparisonApi;
	}

	@Override
	public ItemPropertyApi itemProperty() {
		if (itemPropertyApi == null) {
			itemPropertyApi = new ItemPropertyApiImpl(componentConverter);
		}
		return itemPropertyApi;
	}

	@Override
	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}
}
