package com.sigmundgranaas.forgero.common.api.item;

import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * API for resolving Forgero properties from ItemStacks.
 * <p>
 * This API provides a simplified interface for property resolution that:
 * <ul>
 *     <li>Handles ItemStack to Component conversion internally</li>
 *     <li>Returns empty lists for null/empty/non-Forgero items (no exceptions)</li>
 *     <li>Works with any property engine that returns List&lt;T&gt;</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * ItemPropertyApi props = ForgeroApi.itemProperty();
 *
 * // Resolve OnHit properties
 * List<OnHitProperty> onHitProps = props.get(stack, OnHitProperty.KEY);
 *
 * }</pre>
 *
 * <h2>Design Notes</h2>
 * <p>This API is specifically designed for property engines that return {@code List<P>}.
 * All standard Forgero property engines (OnHit, OnTick, OnHitBlock, etc.) follow this pattern.
 *
 * <p>Resolution is a pure compile-time operation: dynamic conditions are carried on the
 * returned properties as data. Call sites that hold live game state filter the returned
 * list via {@link com.sigmundgranaas.forgero.common.runtime.RuntimeConditions}.
 *
 * @see com.sigmundgranaas.forgero.common.api.ForgeroApi#itemProperty()
 */
public interface ItemPropertyApi {

	/**
	 * Reads the compiled property list of a type directly from a terminal item's artifact.
	 * <p>
	 * This is the preferred runtime accessor: it needs only the property's {@link ResolutionKey},
	 * not its engine, and is an O(1) read of the pre-compiled artifact (no tree traversal). The
	 * returned list includes properties carrying dynamic conditions as data; filter them with
	 * {@link com.sigmundgranaas.forgero.common.runtime.RuntimeConditions} at the call site.
	 *
	 * @param stack The ItemStack to read from
	 * @param key   The property type's resolution key (e.g. {@code OnHitProperty.KEY})
	 * @param <P>   The property type
	 * @return The compiled properties, or empty for null/empty/non-Forgero/non-terminal items
	 */
	<P> List<P> get(ItemStack stack, ResolutionKey<List<P>> key);

}
