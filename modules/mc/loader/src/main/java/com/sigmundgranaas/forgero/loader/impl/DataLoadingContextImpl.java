// DataLoadingContextImpl.java
package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class DataLoadingContextImpl implements DataLoadingContext {
	private ComponentRegistry componentRegistry;
	private TaggedRegistry<Component> taggedComponentRegistry;
	private Resolver resolver;
	private ComponentConverter componentConverter;
	private ComponentNbtConverter nbtConverter;
	private ForgeroDataBundle dataBundle;

	public void initialize(
			ComponentRegistry componentRegistry,
			TaggedRegistry<Component> taggedComponentRegistry,
			Resolver resolver,
			ComponentConverter componentConverter,
			ComponentNbtConverter nbtConverter,
			ForgeroDataBundle dataBundle) {
		this.componentRegistry = componentRegistry;
		this.taggedComponentRegistry = taggedComponentRegistry;
		this.resolver = resolver;
		this.componentConverter = componentConverter;
		this.nbtConverter = nbtConverter;
		this.dataBundle = dataBundle;
	}

	@Override
	public ComponentRegistry getComponentRegistry() {
		return componentRegistry;
	}

	@Override
	public TaggedRegistry<Component> getTaggedComponentRegistry() {
		return taggedComponentRegistry;
	}

	@Override
	public Resolver getResolver() {
		return resolver;
	}

	@Override
	public Optional<Component> getComponent(ItemStack stack) {
		return componentConverter.toComponent(stack);
	}

	@Override
	public ComponentConverter getConverter() {
		return componentConverter;
	}

	@Override
	public ComponentNbtConverter getNbtConverter() {
		return nbtConverter;
	}

	@Override
	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}
}
