package com.sigmundgranaas.repairkit;

import com.sigmundgranaas.repairkit.compat.RepairMaterialProvider;
import com.sigmundgranaas.repairkit.compat.VanillaRepairMaterialProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.*;

/**
 * Registry for repair material providers.
 *
 * Providers are sorted by priority (highest first) and checked in order
 * until one returns a repair ingredient for the given item.
 */
public final class RepairMaterialProviders {

    private static final List<RepairMaterialProvider> providers = new ArrayList<>();
    private static boolean sorted = false;

    private RepairMaterialProviders() {
    }

    /**
     * Register the default providers.
     */
    public static void register() {
        // Always register vanilla provider
        registerProvider(VanillaRepairMaterialProvider.INSTANCE);
        RepairKitMod.LOGGER.info("Registered vanilla repair material provider");

        // Conditionally register Forgero provider if Forgero is loaded
        if (isForgeroLoaded()) {
            try {
                registerForgeroProvider();
                RepairKitMod.LOGGER.info("Registered Forgero repair material provider");
            } catch (Exception e) {
                RepairKitMod.LOGGER.warn("Failed to register Forgero provider: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if Forgero mod is loaded.
     */
    private static boolean isForgeroLoaded() {
        return FabricLoader.getInstance().isModLoaded("forgero") ||
               FabricLoader.getInstance().isModLoaded("vanilla-upgrades");
    }

    /**
     * Register the Forgero provider.
     * This is in a separate method to avoid class loading issues when Forgero is not present.
     */
    private static void registerForgeroProvider() {
        // Use reflection-style loading to avoid NoClassDefFoundError when Forgero is absent
        com.sigmundgranaas.repairkit.compat.ForgeroRepairMaterialProvider provider =
                com.sigmundgranaas.repairkit.compat.ForgeroRepairMaterialProvider.INSTANCE;
        registerProvider(provider);
    }

    /**
     * Register a new repair material provider.
     * Call this during mod initialization.
     *
     * @param provider The provider to register
     */
    public static void registerProvider(RepairMaterialProvider provider) {
        providers.add(provider);
        sorted = false;
    }

    /**
     * Get the repair ingredient for the given item stack.
     * Checks all registered providers in priority order.
     *
     * @param stack The item stack to get repair ingredient for
     * @return Optional containing the repair ingredient, or empty if no provider handles this item
     */
    public static Optional<Ingredient> getRepairIngredient(ItemStack stack) {
        ensureSorted();

        for (RepairMaterialProvider provider : providers) {
            Optional<Ingredient> ingredient = provider.getRepairIngredient(stack);
            if (ingredient.isPresent()) {
                return ingredient;
            }
        }

        return Optional.empty();
    }

    /**
     * Check if the given item can be repaired (has a known repair ingredient).
     *
     * @param stack The item stack to check
     * @return true if the item has a known repair ingredient
     */
    public static boolean canRepair(ItemStack stack) {
        if (!stack.isDamageable()) {
            return false;
        }
        return getRepairIngredient(stack).isPresent();
    }

    private static void ensureSorted() {
        if (!sorted) {
            providers.sort(Comparator.comparingInt(RepairMaterialProvider::getPriority).reversed());
            sorted = true;
        }
    }
}
