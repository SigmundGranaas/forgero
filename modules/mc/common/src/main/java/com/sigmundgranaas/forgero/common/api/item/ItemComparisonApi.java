package com.sigmundgranaas.forgero.common.api.item;

import net.minecraft.item.ItemStack;

/**
 * Comparison API for ItemStack type and similarity checking.
 * <p>
 * This API provides operations for comparing ItemStacks to determine if they are the same type
 * or structurally similar. This is useful for inventory management, item sorting, and crafting systems.
 *
 * <h2>Design Principles</h2>
 * <ul>
 *     <li><b>Type-based comparison:</b> Ignores NBT state when checking type equality</li>
 *     <li><b>Structure-based similarity:</b> Compares base composition, ignoring upgrades</li>
 *     <li><b>Null-safe:</b> Handles null gracefully, returning false</li>
 *     <li><b>Empty-safe:</b> Handles empty ItemStacks gracefully</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * ItemComparisonApi compare = ForgeroApi.itemComparison();
 *
 * // Check if two items are the same type (ignoring NBT)
 * ItemStack pickaxe1 = getCustomPickaxe(); // Iron pickaxe with ruby gem
 * ItemStack pickaxe2 = getCustomPickaxe(); // Iron pickaxe with sapphire gem
 *
 * if (compare.isSameType(pickaxe1, pickaxe2)) {
 *     // Both are the same base pickaxe type
 *     // true - both are pickaxes with iron head
 * }
 *
 * // Check if two items are similar (same structure, different upgrades OK)
 * ItemStack sword1 = getSword(); // Iron sword with ruby gem and fire binding
 * ItemStack sword2 = getSword(); // Iron sword with no upgrades
 *
 * if (compare.areSimilar(sword1, sword2)) {
 *     // Both have the same blade and handle, upgrades differ
 *     // true - both have iron blade and oak handle
 * }
 *
 * // Different materials = not similar
 * ItemStack ironPickaxe = getIronPickaxe();
 * ItemStack diamondPickaxe = getDiamondPickaxe();
 *
 * boolean similar = compare.areSimilar(ironPickaxe, diamondPickaxe);
 * // false - different head materials
 * }</pre>
 *
 * <h2>Comparison Semantics</h2>
 *
 * <h3>isSameType()</h3>
 * <p>
 * Compares items at the type level, ignoring all NBT data (including upgrades and damage).
 * This is equivalent to checking if two ItemStacks would stack if they weren't customized.
 * </p>
 * <ul>
 *     <li>Same item type</li>
 *     <li>Ignores NBT data</li>
 *     <li>Ignores upgrades</li>
 *     <li>Ignores durability/damage</li>
 * </ul>
 *
 * <h3>areSimilar()</h3>
 * <p>
 * Compares items at the structural level - same base parts (head, handle, etc.)
 * but upgrades may differ. This is useful for grouping items by their base composition.
 * </p>
 * <ul>
 *     <li>Same structure parts (head, handle, blade, etc.)</li>
 *     <li>Same material for each part</li>
 *     <li>Upgrades can differ</li>
 *     <li>Durability/damage can differ</li>
 * </ul>
 *
 * @see com.sigmundgranaas.forgero.loader.api.ForgeroApi#itemComparison()
 */
public interface ItemComparisonApi {

	/**
	 * Checks if two ItemStacks are the same type, ignoring NBT state.
	 * <p>
	 * This comparison ignores all NBT data including upgrades, damage, and custom names.
	 * It only checks if the two items are fundamentally the same type.
	 * </p>
	 * <p>
	 * For Forgero items, this compares the base component identifier.
	 * For vanilla items, this compares the Item type.
	 * </p>
	 *
	 * <h3>Returns true when</h3>
	 * <ul>
	 *     <li>Both are the same Forgero component type (e.g., both "forgero:iron-pickaxe")</li>
	 *     <li>Both are the same vanilla Item type</li>
	 * </ul>
	 *
	 * <h3>Returns false when</h3>
	 * <ul>
	 *     <li>Different component types or Item types</li>
	 *     <li>Either stack is null or empty</li>
	 *     <li>One is Forgero, the other is vanilla (even if similar items)</li>
	 * </ul>
	 *
	 * @param stack1 The first ItemStack to compare
	 * @param stack2 The second ItemStack to compare
	 * @return true if both stacks are the same type, false otherwise
	 */
	boolean isSameType(ItemStack stack1, ItemStack stack2);

	/**
	 * Checks if two ItemStacks are structurally similar.
	 * <p>
	 * This comparison checks if two items have the same base structure (same parts with same materials)
	 * but allows upgrades to differ. This is useful for grouping items that are "the same sword, just
	 * with different gems."
	 * </p>
	 * <p>
	 * For example, an iron sword with a ruby gem is similar to an iron sword with a sapphire gem,
	 * but not similar to a diamond sword (even if both have the same gems).
	 * </p>
	 *
	 * <h3>Returns true when</h3>
	 * <ul>
	 *     <li>Both items have the same structure parts (head, handle, blade, etc.)</li>
	 *     <li>Each corresponding part is made of the same material</li>
	 *     <li>Upgrades may differ (gems, bindings, etc.)</li>
	 *     <li>Durability/damage may differ</li>
	 * </ul>
	 *
	 * <h3>Returns false when</h3>
	 * <ul>
	 *     <li>Different number of structure parts</li>
	 *     <li>Different part types (pickaxe head vs sword blade)</li>
	 *     <li>Different materials for corresponding parts (iron head vs diamond head)</li>
	 *     <li>Either stack is null or empty</li>
	 *     <li>Either stack is not a Forgero item</li>
	 * </ul>
	 *
	 * @param stack1 The first ItemStack to compare
	 * @param stack2 The second ItemStack to compare
	 * @return true if both stacks have the same structure, false otherwise
	 */
	boolean areSimilar(ItemStack stack1, ItemStack stack2);
}
