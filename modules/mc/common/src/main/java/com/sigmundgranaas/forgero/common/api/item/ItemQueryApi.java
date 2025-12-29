package com.sigmundgranaas.forgero.common.api.item;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only query API for ItemStack operations.
 * <p>
 * This API provides a Minecraft-native interface for querying Forgero item properties
 * without exposing internal Component abstractions. All methods are safe to call at any time
 * and will never modify the provided ItemStack.
 *
 * <h2>Design Principles</h2>
 * <ul>
 *     <li><b>ItemStack-first:</b> Works with ItemStack, not internal Components</li>
 *     <li><b>Null-safe:</b> All methods handle null gracefully, returning sensible defaults</li>
 *     <li><b>Empty-safe:</b> Empty ItemStacks are handled without exceptions</li>
 *     <li><b>Default values:</b> Returns 0/false/empty for non-Forgero items instead of throwing exceptions</li>
 *     <li><b>Read-only:</b> Never modifies the input ItemStack</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * ItemQueryApi query = ForgeroApi.itemQuery();
 *
 * // Check if item is customizable and get properties
 * if (query.isCustomizable(stack)) {
 *     float damage = query.getAttackDamage(stack);
 *     int durability = query.getMaxDurability(stack);
 *     List<ItemStack> parts = query.getParts(stack);
 * }
 *
 * // Query attributes with automatic defaults
 * float miningSpeed = query.getMiningSpeed(vanillaPickaxe); // Returns 0.0f
 *
 * // Check tags
 * if (query.hasTag(stack, OpenIdentifier.of("forgero:pickaxe"))) {
 *     // Handle pickaxe-specific logic
 * }
 * }</pre>
 *
 * <h2>Return Value Semantics</h2>
 * <ul>
 *     <li><b>Numeric queries:</b> Return 0 or 0.0f for non-Forgero items, null, or empty stacks</li>
 *     <li><b>Boolean queries:</b> Return false for non-Forgero items, null, or empty stacks</li>
 *     <li><b>Collection queries:</b> Return empty collections (List, Set) for non-Forgero items</li>
 *     <li><b>Optional queries:</b> Return Optional.empty() when value is truly optional</li>
 * </ul>
 *
 * @see com.sigmundgranaas.forgero.loader.api.ForgeroApi#itemQuery()
 */
public interface ItemQueryApi {

	// ========== Basic Queries ==========

	/**
	 * Checks if the provided ItemStack represents a Forgero item.
	 * <p>
	 * This is useful for determining if other query methods will return meaningful values
	 * versus defaults.
	 *
	 * @param stack The ItemStack to check
	 * @return true if the stack represents a Forgero item, false otherwise (including null/empty)
	 */
	boolean isForgeroItem(ItemStack stack);

	/**
	 * Checks if the provided ItemStack can be customized with upgrades.
	 * <p>
	 * Customizable items have upgrade slots that can accept gems, bindings, or other enhancements.
	 *
	 * @param stack The ItemStack to check
	 * @return true if the item has upgrade slots, false otherwise (including null/empty/non-Forgero items)
	 */
	boolean isCustomizable(ItemStack stack);

	/**
	 * Checks if the provided ItemStack has any empty upgrade slots.
	 *
	 * @param stack The ItemStack to check
	 * @return true if the item has at least one empty upgrade slot, false otherwise
	 */
	boolean hasEmptySlots(ItemStack stack);

	/**
	 * Checks if all upgrade slots in the provided ItemStack are filled.
	 *
	 * @param stack The ItemStack to check
	 * @return true if all upgrade slots are filled, false otherwise (including items with no slots)
	 */
	boolean isFullyUpgraded(ItemStack stack);

	/**
	 * Returns the total number of parts (structure + upgrades) in the provided ItemStack.
	 * <p>
	 * For example, a pickaxe with a head, handle, and 2 gems would return 4.
	 *
	 * @param stack The ItemStack to query
	 * @return The number of parts, or 0 for null/empty/non-Forgero items
	 */
	int getPartCount(ItemStack stack);

	// ========== Attribute Queries ==========

	/**
	 * Returns the current durability of the provided ItemStack.
	 * <p>
	 * This reflects the item's current damage state, not its maximum durability.
	 *
	 * @param stack The ItemStack to query
	 * @return The current durability, or 0 for null/empty/non-Forgero items
	 */
	int getDurability(ItemStack stack);

	/**
	 * Returns the maximum durability of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The maximum durability, or 0 for null/empty/non-Forgero items
	 */
	int getMaxDurability(ItemStack stack);

	/**
	 * Returns the attack damage value of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The attack damage, or 0.0f for null/empty/non-Forgero items
	 */
	float getAttackDamage(ItemStack stack);

	/**
	 * Returns the mining speed value of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The mining speed, or 0.0f for null/empty/non-Forgero items
	 */
	float getMiningSpeed(ItemStack stack);

	/**
	 * Returns the attack speed value of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The attack speed, or 0.0f for null/empty/non-Forgero items
	 */
	float getAttackSpeed(ItemStack stack);

	/**
	 * Returns the mining level (harvest level) of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The mining level, or 0 for null/empty/non-Forgero items
	 */
	int getMiningLevel(ItemStack stack);

	/**
	 * Returns the armor value of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The armor value, or 0 for null/empty/non-Forgero items
	 */
	int getArmor(ItemStack stack);

	/**
	 * Returns the armor toughness value of the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The armor toughness, or 0.0f for null/empty/non-Forgero items
	 */
	float getArmorToughness(ItemStack stack);

	/**
	 * Returns the value of a generic attribute for the provided ItemStack.
	 * <p>
	 * This is the generic version of the specific attribute getters above.
	 * Use this for custom attributes or when the attribute type is determined at runtime.
	 *
	 * @param stack         The ItemStack to query
	 * @param attributeType The attribute identifier (e.g., "forgero:attack_damage")
	 * @return The attribute value, or 0.0f if not found/null/empty
	 */
	float getAttribute(ItemStack stack, OpenIdentifier attributeType);

	// ========== Composition Queries ==========

	/**
	 * Returns all child components (parts + upgrades) as ItemStacks.
	 * <p>
	 * For example, a pickaxe might return [head, handle, gem1, gem2].
	 *
	 * @param stack The ItemStack to query
	 * @return List of component ItemStacks, or empty list for null/empty/non-Forgero items
	 */
	List<ItemStack> getComponents(ItemStack stack);

	/**
	 * Returns only the structure parts (not upgrades) as ItemStacks.
	 * <p>
	 * Structure parts are the required components that define the item's base composition.
	 * For example, a pickaxe would return [head, handle] but not gems.
	 *
	 * @param stack The ItemStack to query
	 * @return List of part ItemStacks, or empty list for null/empty/non-Forgero items
	 */
	List<ItemStack> getParts(ItemStack stack);

	/**
	 * Returns all installed upgrades as ItemStacks.
	 * <p>
	 * Upgrades are optional enhancements like gems or bindings that have been added to upgrade slots.
	 *
	 * @param stack The ItemStack to query
	 * @return List of upgrade ItemStacks, or empty list for null/empty/non-Forgero items/no upgrades
	 */
	List<ItemStack> getInstalledUpgrades(ItemStack stack);

	/**
	 * Returns the primary material identifier of the provided ItemStack.
	 * <p>
	 * The primary material is typically the material of the main component
	 * (e.g., "forgero:iron" for an iron pickaxe head).
	 *
	 * @param stack The ItemStack to query
	 * @return Optional containing the material identifier, or empty for null/empty/non-Forgero items
	 */
	Optional<OpenIdentifier> getPrimaryMaterial(ItemStack stack);

	// ========== Slot Information ==========

	/**
	 * Returns the total number of upgrade slots in the provided ItemStack.
	 * <p>
	 * This includes both filled and empty slots.
	 *
	 * @param stack The ItemStack to query
	 * @return The total number of upgrade slots, or 0 for null/empty/non-Forgero items
	 */
	int getUpgradeSlotCount(ItemStack stack);

	/**
	 * Returns the number of filled upgrade slots in the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The number of filled slots, or 0 for null/empty/non-Forgero items
	 */
	int getFilledSlotCount(ItemStack stack);

	/**
	 * Returns the number of empty upgrade slots in the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return The number of empty slots, or 0 for null/empty/non-Forgero items
	 */
	int getEmptySlotCount(ItemStack stack);

	// ========== Tag Queries ==========

	/**
	 * Checks if the provided ItemStack has the specified tag.
	 * <p>
	 * Tags are used for categorization and slot type matching
	 * (e.g., "forgero:pickaxe", "forgero:gem").
	 *
	 * @param stack The ItemStack to check
	 * @param tag   The tag identifier to check for
	 * @return true if the item has the tag, false otherwise (including null/empty)
	 */
	boolean hasTag(ItemStack stack, OpenIdentifier tag);

	/**
	 * Returns all tags associated with the provided ItemStack.
	 *
	 * @param stack The ItemStack to query
	 * @return Set of tag identifiers, or empty set for null/empty/non-Forgero items
	 */
	Set<OpenIdentifier> getTags(ItemStack stack);
}
