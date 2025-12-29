package com.sigmundgranaas.repairkit.recipe;

import com.sigmundgranaas.repairkit.RepairKitRecipes;
import com.sigmundgranaas.repairkit.RepairMaterialProviders;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Recipe for repairing items using a filled repair kit.
 *
 * Pattern: Filled repair kit + damaged item
 * Result: Repaired item (kit is returned with one less material)
 */
public class RepairKitRepairRecipe extends SpecialCraftingRecipe {

    public RepairKitRepairRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        ItemStack kitStack = ItemStack.EMPTY;
        ItemStack toolStack = ItemStack.EMPTY;
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
            } else if (stack.isDamageable()) {
                if (!toolStack.isEmpty()) {
                    return false; // Only one tool allowed
                }
                toolStack = stack;
            } else {
                return false; // Unknown item
            }
        }

        // Need exactly one kit and one tool
        if (kitStack.isEmpty() || toolStack.isEmpty() || itemCount != 2) {
            return false;
        }

        // Kit must have materials
        if (RepairKitItem.isEmpty(kitStack)) {
            return false;
        }

        // Tool must be damaged
        if (!toolStack.isDamaged()) {
            return false;
        }

        // Check if stored material matches tool's repair ingredient
        return canRepairWith(kitStack, toolStack);
    }

    private boolean canRepairWith(ItemStack kitStack, ItemStack toolStack) {
        Optional<Identifier> storedMaterial = RepairKitItem.getStoredMaterial(kitStack);
        if (storedMaterial.isEmpty()) {
            return false;
        }

        Optional<Ingredient> repairIngredient = RepairMaterialProviders.getRepairIngredient(toolStack);
        if (repairIngredient.isEmpty()) {
            return false;
        }

        Item materialItem = Registries.ITEM.get(storedMaterial.get());
        ItemStack materialStack = new ItemStack(materialItem);
        return repairIngredient.get().test(materialStack);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack kitStack = ItemStack.EMPTY;
        ItemStack toolStack = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof RepairKitItem) {
                kitStack = stack;
            } else if (stack.isDamageable()) {
                toolStack = stack;
            }
        }

        if (kitStack.isEmpty() || toolStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Get the tier and calculate repair amount
        RepairKitItem kitItem = (RepairKitItem) kitStack.getItem();
        int repairAmount = kitItem.getTier().calculateRepairAmount(toolStack.getMaxDamage());

        // Create repaired tool
        ItemStack result = toolStack.copy();
        int newDamage = Math.max(0, result.getDamage() - repairAmount);
        result.setDamage(newDamage);

        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.getItem() instanceof RepairKitItem) {
                // Return kit with one less material
                ItemStack kitRemainder = stack.copy();
                RepairKitItem.useMaterial(kitRemainder);

                // Only return if there's something left
                if (RepairKitItem.getMaterialCount(kitRemainder) > 0 ||
                        RepairKitItem.getStoredMaterial(kitRemainder).isEmpty()) {
                    remainder.set(i, kitRemainder);
                } else {
                    // Empty kit (no materials, but had some)
                    ItemStack emptyKit = new ItemStack(stack.getItem());
                    remainder.set(i, emptyKit);
                }
            }
        }

        return remainder;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RepairKitRecipes.REPAIR_RECIPE_SERIALIZER;
    }
}
