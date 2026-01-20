package com.sigmundgranaas.forgero.common.api.item;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import net.minecraft.item.ItemStack;

/**
 * Mutation API for ItemStack modification operations.
 * <p>
 * This API provides operations that modify ItemStacks and return new instances.
 * All methods follow immutability principles - the original ItemStack is never modified.
 *
 * <h2>Design Principles</h2>
 * <ul>
 *     <li><b>Immutability:</b> Always returns new ItemStack, never modifies the original</li>
 *     <li><b>Failure handling:</b> Returns original ItemStack on failure (no exceptions)</li>
 *     <li><b>Null-safe:</b> Handles null gracefully, returning the original stack</li>
 *     <li><b>Empty-safe:</b> Handles empty ItemStacks gracefully</li>
 *     <li><b>NBT persistence:</b> All mutations are persisted via NBT serialization</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * ItemMutationApi mutate = ForgeroApi.itemMutation();
 *
 * // Install an upgrade (gem, binding, etc.)
 * ItemStack upgraded = mutate.installUpgrade(tool, gem);
 * if (upgraded != tool) {
 *     // Installation succeeded - upgraded is a new ItemStack
 *     player.setStackInHand(Hand.MAIN_HAND, upgraded);
 * }
 *
 * // Check compatibility before attempting installation
 * if (mutate.canInstallUpgrade(tool, upgrade)) {
 *     ItemStack result = mutate.installUpgrade(tool, upgrade);
 *     // Installation will succeed
 * }
 *
 * // Remove a specific upgrade by ID
 * OpenIdentifier upgradeId = OpenIdentifier.parse("forgero:ruby_gem");
 * ItemStack withoutGem = mutate.removeUpgrade(tool, upgradeId);
 *
 * // Remove all upgrades
 * ItemStack base = mutate.removeAllUpgrades(customizedTool);
 * }</pre>
 *
 * <h2>Immutability Contract</h2>
 * <p>
 * All mutation methods guarantee that the original ItemStack passed as a parameter
 * is never modified. A new ItemStack is always returned. On failure, the original
 * ItemStack reference is returned unchanged.
 * </p>
 * <pre>{@code
 * ItemStack original = getPickaxe();
 * ItemStack result = mutate.installUpgrade(original, gem);
 *
 * // original == result only if installation failed
 * // original != result if installation succeeded
 * // In both cases, the content of 'original' stack is unchanged
 * }</pre>
 *
 * <h2>Failure Semantics</h2>
 * <ul>
 *     <li><b>Incompatible upgrades:</b> Returns original stack</li>
 *     <li><b>No empty slots:</b> Returns original stack</li>
 *     <li><b>Null/empty inputs:</b> Returns original stack (or empty for null)</li>
 *     <li><b>Non-Forgero items:</b> Returns original stack</li>
 *     <li><b>Invalid upgrade ID:</b> Returns original stack</li>
 * </ul>
 *
 * @see com.sigmundgranaas.forgero.common.api.ForgeroApi#itemMutation()
 */
public interface ItemMutationApi {

	// ========== Upgrade Installation ==========

	/**
	 * Installs an upgrade into the first compatible slot of the target ItemStack.
	 * <p>
	 * This method finds the first empty upgrade slot that is compatible with the provided
	 * upgrade and installs it. The upgrade's compatibility is determined by tag matching.
	 *
	 * <h3>Success Conditions</h3>
	 * <ul>
	 *     <li>Target is a customizable Forgero item</li>
	 *     <li>Target has at least one empty upgrade slot</li>
	 *     <li>Upgrade is compatible with at least one empty slot</li>
	 *     <li>Both target and upgrade are non-null and non-empty</li>
	 * </ul>
	 *
	 * @param target  The ItemStack to install the upgrade into
	 * @param upgrade The upgrade ItemStack to install (gem, binding, etc.)
	 * @return A new ItemStack with the upgrade installed, or the original if installation failed
	 */
	ItemStack installUpgrade(ItemStack target, ItemStack upgrade);

	/**
	 * Removes an upgrade from the target ItemStack by its component identifier.
	 * <p>
	 * The upgrade ID should match the identifier of the installed upgrade component
	 * (e.g., "forgero:ruby_gem").
	 *
	 * @param target    The ItemStack to remove the upgrade from
	 * @param upgradeId The identifier of the upgrade to remove
	 * @return A new ItemStack with the upgrade removed, or the original if removal failed
	 */
	ItemStack removeUpgrade(ItemStack target, OpenIdentifier upgradeId);

	/**
	 * Removes all installed upgrades from the target ItemStack.
	 * <p>
	 * This clears all upgrade slots, returning the item to its base state with only
	 * its structure parts (head, handle, etc.) remaining.
	 *
	 * @param target The ItemStack to remove all upgrades from
	 * @return A new ItemStack with all upgrades removed, or the original if operation failed
	 */
	ItemStack removeAllUpgrades(ItemStack target);

	// ========== Compatibility Checking ==========

	/**
	 * Checks if an upgrade can be installed into the target ItemStack.
	 * <p>
	 * This performs the same compatibility checks as {@link #installUpgrade(ItemStack, ItemStack)}
	 * but without actually performing the installation. Use this to validate compatibility
	 * before attempting installation.
	 *
	 * <h3>Returns true when</h3>
	 * <ul>
	 *     <li>Target is a customizable Forgero item</li>
	 *     <li>Target has at least one empty upgrade slot</li>
	 *     <li>Upgrade is compatible with at least one empty slot (tag matching)</li>
	 *     <li>Both target and upgrade are non-null and non-empty</li>
	 * </ul>
	 *
	 * @param target  The ItemStack to check compatibility for
	 * @param upgrade The upgrade ItemStack to check
	 * @return true if the upgrade can be installed, false otherwise
	 */
	boolean canInstallUpgrade(ItemStack target, ItemStack upgrade);
}
