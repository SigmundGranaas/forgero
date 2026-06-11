package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * The single entry point every property event uses to obtain the active properties of an item.
 *
 * <p>It folds the two steps every property manager shared — read the compiled property list
 * (O(1), by key, from the terminal's artifact) and filter it by the live {@link DynamicContext}
 * via {@link RuntimeConditions} — into one call. Managers keep only what is genuinely unique to
 * their event: how they build the context and what they do with each active property.
 *
 * <p>No tree traversal, no engine, no per-event resolution: the properties were compiled once at
 * construction (see {@code docs/ADR-002-compiler-in-the-factory.md}); this reads them and applies
 * the runtime conditions that the compile phase deliberately left as data.
 */
public final class PropertyDispatcher {

	private PropertyDispatcher() {
	}

	/**
	 * @return the properties of the given type on {@code stack} whose dynamic conditions pass
	 * against {@code context}; empty for null/empty/non-Forgero items.
	 */
	public static <P extends ConditionalProperty> List<P> active(ItemStack stack, ResolutionKey<List<P>> key, DynamicContext context) {
		return RuntimeConditions.filter(ForgeroApi.itemProperty().get(stack, key), context);
	}
}
