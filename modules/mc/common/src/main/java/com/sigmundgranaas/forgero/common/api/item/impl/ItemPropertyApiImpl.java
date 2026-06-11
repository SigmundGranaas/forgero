package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implementation of {@link ItemPropertyApi}.
 * <p>
 * This implementation delegates to ComponentConverter for ItemStack-to-Component conversion
 * and returns empty lists for non-Forgero items, ensuring null-safe behavior.
 */
public class ItemPropertyApiImpl implements ItemPropertyApi {

	private final ComponentConverter converter;

	/**
	 * Creates a new ItemPropertyApiImpl.
	 *
	 * @param converter The component converter for ItemStack-to-Component conversion
	 */
	public ItemPropertyApiImpl(ComponentConverter converter) {
		this.converter = converter;
	}

	@Override
	public <P> List<P> resolve(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}

		return converter.toComponent(stack)
				.map(component -> {
					DataTypeEngine<?, List<P>> engine = engineSupplier.get();
					return engine.resolve(component);
				})
				.orElse(Collections.emptyList());
	}

	@Override
	public <P> List<P> resolve(ItemStack stack, DataTypeEngine<?, List<P>> engine) {
		if (stack == null || stack.isEmpty() || engine == null) {
			return Collections.emptyList();
		}

		return converter.toComponent(stack)
				.map(component -> engine.resolve(component))
				.orElse(Collections.emptyList());
	}
}
