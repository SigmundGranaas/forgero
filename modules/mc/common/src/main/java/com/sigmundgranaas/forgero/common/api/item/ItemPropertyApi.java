package com.sigmundgranaas.forgero.common.api.item;

import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

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
 * List<OnHitProperty> onHitProps = props.resolve(stack, OnHitProperty.Engine::new);
 *
 * // With pre-created engine
 * OnHitProperty.Engine engine = new OnHitProperty.Engine();
 * List<OnHitProperty> props = props.resolve(stack, engine);
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
	 * Resolves properties from an ItemStack using the given engine supplier.
	 * <p>
	 * This is the most convenient form - pass a method reference to the engine constructor:
	 * <pre>{@code
	 * List<OnHitProperty> props = api.resolve(stack, OnHitProperty.Engine::new);
	 * }</pre>
	 *
	 * @param stack           The ItemStack to resolve properties from
	 * @param engineSupplier  Supplier that creates the property engine (e.g., {@code OnHitProperty.Engine::new})
	 * @param <P>             The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	<P> List<P> resolve(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier);

	/**
	 * Resolves properties from an ItemStack using a pre-created engine.
	 * <p>
	 * Use this when you need to reuse the same engine instance across multiple resolutions.
	 *
	 * @param stack   The ItemStack to resolve properties from
	 * @param engine  The property engine to use for resolution
	 * @param <P>     The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	<P> List<P> resolve(ItemStack stack, DataTypeEngine<?, List<P>> engine);

	/**
	 * Checks if the ItemStack has any properties of the given type.
	 *
	 * @param stack          The ItemStack to check
	 * @param engineSupplier Supplier that creates the property engine
	 * @param <P>            The property type
	 * @return true if the item has at least one property of the given type
	 */
	default <P> boolean hasProperties(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier) {
		return !resolve(stack, engineSupplier).isEmpty();
	}
}
