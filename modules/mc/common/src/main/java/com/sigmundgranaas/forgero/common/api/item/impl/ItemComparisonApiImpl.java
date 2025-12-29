package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import net.minecraft.item.ItemStack;

/**
 * Package-private implementation of {@link ItemComparisonApi}.
 * <p>
 * This implementation delegates to ComponentConverter for type-based comparisons
 * and handles null/empty safety.
 */
public class ItemComparisonApiImpl implements ItemComparisonApi {

	private final ComponentConverter converter;

	/**
	 * Creates a new ItemComparisonApiImpl instance.
	 *
	 * @param converter The component converter
	 */
	public ItemComparisonApiImpl(ComponentConverter converter) {
		this.converter = converter;
	}

	@Override
	public boolean isSameType(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack1.isEmpty() || stack2 == null || stack2.isEmpty()) {
			return false;
		}

		// For Forgero items, compare component IDs (stateless)
		var id1 = converter.toComponentId(stack1);
		var id2 = converter.toComponentId(stack2);

		// Both must be Forgero items
		if (id1.isEmpty() || id2.isEmpty()) {
			// If either is not a Forgero item, compare vanilla item types
			return stack1.getItem() == stack2.getItem();
		}

		// Compare Forgero component IDs
		return id1.get().equals(id2.get());
	}

	@Override
	public boolean areSimilar(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack1.isEmpty() || stack2 == null || stack2.isEmpty()) {
			return false;
		}

		var comp1 = converter.toComponent(stack1);
		var comp2 = converter.toComponent(stack2);

		// Both must be Forgero items
		if (comp1.isEmpty() || comp2.isEmpty()) {
			return false;
		}

		var component1 = comp1.get();
		var component2 = comp2.get();

		// Compare structure parts (ignoring upgrades)
		var children1 = component1.getChildren();
		var children2 = component2.getChildren();

		// Must have same number of structure parts
		if (children1.size() != children2.size()) {
			return false;
		}

		// If no children, compare component IDs directly
		if (children1.isEmpty()) {
			return component1.id().equals(component2.id());
		}

		// Compare each child component ID (structure parts)
		for (int i = 0; i < children1.size(); i++) {
			var child1 = children1.get(i);
			var child2 = children2.get(i);
			if (!child1.id().equals(child2.id())) {
				return false;
			}
		}

		return true;
	}
}
