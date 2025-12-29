package com.sigmundgranaas.repairkit.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.Optional;

/**
 * Interface for providing repair material information for items.
 *
 * Implementations can provide repair ingredient information for different
 * item types (vanilla tools, armor, modded items, etc.).
 *
 * The priority system allows mods to override default behavior - higher
 * priority providers are checked first.
 */
public interface RepairMaterialProvider {

    /**
     * Get the repair ingredient for the given item stack.
     *
     * @param stack The item stack to get repair ingredient for
     * @return Optional containing the repair ingredient, or empty if this provider doesn't handle this item
     */
    Optional<Ingredient> getRepairIngredient(ItemStack stack);

    /**
     * Get the priority of this provider.
     * Higher values are checked first. Default vanilla provider uses priority 0.
     *
     * @return The priority value
     */
    int getPriority();
}
