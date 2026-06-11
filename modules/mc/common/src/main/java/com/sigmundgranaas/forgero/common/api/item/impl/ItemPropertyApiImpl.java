package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
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
	public <P> List<P> resolve(ItemStack stack, Supplier<? extends CompilerPass<List<P>>> engineSupplier) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
				.map(component -> read(component, engineSupplier.get()))
				.orElse(Collections.emptyList());
	}

	@Override
	public <P> List<P> resolve(ItemStack stack, CompilerPass<List<P>> engine) {
		if (stack == null || stack.isEmpty() || engine == null) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
				.map(component -> read(component, engine))
				.orElse(Collections.emptyList());
	}

	private <P> List<P> read(Component component, CompilerPass<List<P>> engine) {
		// Terminal items carry the compiled property list; read it directly.
		if (component instanceof EquipmentComponent equipment) {
			return equipment.properties(engine.key());
		}
		// Fallback: compile on demand for non-terminal components.
		return engine.resolve(component);
	}
}
