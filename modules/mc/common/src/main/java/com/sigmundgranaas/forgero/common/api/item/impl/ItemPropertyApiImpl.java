package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implementation of {@link ItemPropertyApi}.
 * <p>
 * For terminal (equipment) items the property list is read directly from the component's
 * pre-compiled artifact — an O(1) map lookup, no tree traversal or re-compilation. The
 * artifact was produced once at construction by the factory (see
 * {@code docs/ADR-002-compiler-in-the-factory.md}). The on-demand {@code engine.resolve}
 * path remains only as a fallback for the rare non-terminal component. Returns empty lists
 * for null/empty/non-Forgero items.
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
	public <P> List<P> get(ItemStack stack, ResolutionKey<List<P>> key) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
				.map(component -> resolve(component, key))
				.orElse(Collections.emptyList());
	}

	/**
	 * Terminals read their pre-compiled list in O(1); non-terminal components (parts shown in
	 * tooltips/recipes, synthetic items) compile their list on demand by running the registered
	 * {@link CompilerPass} for the key over the tree — the fallback the compile-at-construction
	 * inversion was supposed to keep. Mirrors how attributes fall back to {@code StatFold.fold}.
	 */
	@SuppressWarnings("unchecked")
	private <P> List<P> resolve(Component component, ResolutionKey<List<P>> key) {
		if (component instanceof EquipmentComponent equipment) {
			return equipment.properties(key);
		}
		Supplier<? extends CompilerPass<? extends List<?>>> pass = CompilerPasses.registered().get(key.id());
		if (pass == null) {
			return Collections.emptyList();
		}
		return (List<P>) pass.get().resolve(component);
	}

}
