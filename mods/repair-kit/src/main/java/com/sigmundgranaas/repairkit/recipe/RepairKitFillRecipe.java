package com.sigmundgranaas.repairkit.recipe;

import com.sigmundgranaas.repairkit.RepairKitRecipes;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Recipe for filling a repair kit with materials.
 *
 * Pattern: Empty/partially filled repair kit + stack of materials
 * Result: Filled repair kit with increased material count
 *
 * The materials must match any already stored material in the kit.
 */
public class RepairKitFillRecipe extends SpecialCraftingRecipe {

    public RepairKitFillRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        ItemStack kitStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;
        int itemCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            itemCount++;

            if (stack.getItem() instanceof RepairKitItem) {
                if (!kitStack.isEmpty()) {
                    return false; // Only one kit allowed
                }
                kitStack = stack;
            } else {
                if (!materialStack.isEmpty() && !ItemStack.areItemsEqual(materialStack, stack)) {
                    return false; // Only one type of material allowed
                }
                materialStack = stack;
            }
        }

        // Need exactly one kit and at least one material
        if (kitStack.isEmpty() || materialStack.isEmpty()) {
            return false;
        }

        // Kit must not be full
        if (RepairKitItem.isFull(kitStack)) {
            return false;
        }

        // Material must match stored material (if any)
        var storedMaterial = RepairKitItem.getStoredMaterial(kitStack);
        if (storedMaterial.isPresent()) {
            Identifier materialId = Registries.ITEM.getId(materialStack.getItem());
            if (!storedMaterial.get().equals(materialId)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack kitStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;
        int materialCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof RepairKitItem) {
                kitStack = stack;
            } else {
                materialStack = stack;
                materialCount += stack.getCount();
            }
        }

        if (kitStack.isEmpty() || materialStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create result with filled kit
        ItemStack result = kitStack.copy();
        Identifier materialId = Registries.ITEM.getId(materialStack.getItem());
        int currentCount = RepairKitItem.getMaterialCount(result);
        int toAdd = Math.min(materialCount, RepairKitItem.MAX_CAPACITY - currentCount);

        RepairKitItem.setStoredMaterial(result, materialId, currentCount + toAdd);

        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RepairKitRecipes.FILL_RECIPE_SERIALIZER;
    }
}
