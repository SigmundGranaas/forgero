package com.sigmundgranaas.forgero.common.api.item;

import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
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
 * // Resolve OnHit properties with context
 * DynamicContext ctx = new DynamicContext.Builder()
 *     .put(ContextKeys.TARGET_TAGS, targetTags)
 *     .build();
 * List<OnHitProperty> onHitProps = props.resolve(stack, OnHitProperty.Engine::new, ctx);
 *
 * // Resolve with empty context
 * List<OnTickProperty> tickProps = props.resolve(stack, OnTickProperty.Engine::new);
 *
 * // With pre-created engine
 * OnHitProperty.Engine engine = new OnHitProperty.Engine();
 * List<OnHitProperty> props = props.resolve(stack, engine, ctx);
 * }</pre>
 *
 * <h2>Design Notes</h2>
 * <p>This API is specifically designed for property engines that return {@code List<P>}.
 * All standard Forgero property engines (OnHit, OnTick, OnHitBlock, etc.) follow this pattern.
 *
 * @see com.sigmundgranaas.forgero.common.api.ForgeroApi#itemProperty()
 */
public interface ItemPropertyApi {

	/**
	 * Resolves properties from an ItemStack using the given engine supplier.
	 * <p>
	 * This is the most convenient form - pass a method reference to the engine constructor:
	 * <pre>{@code
	 * List<OnHitProperty> props = api.resolve(stack, OnHitProperty.Engine::new, context);
	 * }</pre>
	 *
	 * @param stack           The ItemStack to resolve properties from
	 * @param engineSupplier  Supplier that creates the property engine (e.g., {@code OnHitProperty.Engine::new})
	 * @param context         The dynamic context for runtime condition evaluation
	 * @param <P>             The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	<P> List<P> resolve(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier, DynamicContext context);

	/**
	 * Resolves properties from an ItemStack using the given engine supplier with empty context.
	 *
	 * @param stack          The ItemStack to resolve properties from
	 * @param engineSupplier Supplier that creates the property engine
	 * @param <P>            The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	default <P> List<P> resolve(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier) {
		return resolve(stack, engineSupplier, DynamicContext.empty());
	}

	/**
	 * Resolves properties from an ItemStack using a pre-created engine.
	 * <p>
	 * Use this when you need to reuse the same engine instance across multiple resolutions.
	 *
	 * @param stack   The ItemStack to resolve properties from
	 * @param engine  The property engine to use for resolution
	 * @param context The dynamic context for runtime condition evaluation
	 * @param <P>     The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	<P> List<P> resolve(ItemStack stack, DataTypeEngine<?, List<P>> engine, DynamicContext context);

	/**
	 * Resolves properties from an ItemStack using a pre-created engine with empty context.
	 *
	 * @param stack  The ItemStack to resolve properties from
	 * @param engine The property engine to use for resolution
	 * @param <P>    The property type
	 * @return List of resolved properties, or empty list for null/empty/non-Forgero items
	 */
	default <P> List<P> resolve(ItemStack stack, DataTypeEngine<?, List<P>> engine) {
		return resolve(stack, engine, DynamicContext.empty());
	}

	/**
	 * Checks if the ItemStack has any properties of the given type.
	 *
	 * @param stack          The ItemStack to check
	 * @param engineSupplier Supplier that creates the property engine
	 * @param context        The dynamic context for runtime condition evaluation
	 * @param <P>            The property type
	 * @return true if the item has at least one property of the given type
	 */
	default <P> boolean hasProperties(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier, DynamicContext context) {
		return !resolve(stack, engineSupplier, context).isEmpty();
	}

	/**
	 * Checks if the ItemStack has any properties of the given type with empty context.
	 *
	 * @param stack          The ItemStack to check
	 * @param engineSupplier Supplier that creates the property engine
	 * @param <P>            The property type
	 * @return true if the item has at least one property of the given type
	 */
	default <P> boolean hasProperties(ItemStack stack, Supplier<? extends DataTypeEngine<?, List<P>>> engineSupplier) {
		return hasProperties(stack, engineSupplier, DynamicContext.empty());
	}
}
