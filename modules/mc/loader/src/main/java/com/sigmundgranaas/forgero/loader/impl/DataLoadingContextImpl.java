package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import net.minecraft.item.ItemStack;

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
	private Resolver resolver;
	private ComponentConverter componentConverter;
	private ComponentNbtConverter nbtConverter;
	private SlotManager slotManager;
	private ForgeroDataBundle dataBundle;

	public void initialize(
			ComponentRegistry componentRegistry,
			TaggedRegistry<Component> taggedComponentRegistry,
			Resolver resolver,
			ComponentConverter componentConverter,
			ComponentNbtConverter nbtConverter,
			SlotManager slotManager,
			ForgeroDataBundle dataBundle) {
		this.componentRegistry = componentRegistry;
		this.taggedComponentRegistry = taggedComponentRegistry;
		this.resolver = resolver;
		this.componentConverter = componentConverter;
		this.nbtConverter = nbtConverter;
		this.slotManager = slotManager;
		this.dataBundle = dataBundle;
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
	public Resolver resolver() {
		return resolver;
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
	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}
}
