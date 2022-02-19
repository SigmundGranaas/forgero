package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.PatternItem;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ToolPartRecipe extends ShapelessRecipe {
    public ToolPartRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
    }


    private int containsPattern(CraftingInventory craftingInventory) {
        boolean matchingPattern = false;
        int index = -1;

        for (int i = 0; i < craftingInventory.size(); i++) {
            if (craftingInventory.getStack(i).getItem() instanceof PatternItem pattern) {
                if (matchingPattern) {
                    index = -1;
                }
                if (!matchingPattern) {
                    matchingPattern = pattern.getType() == getMatchingPattern();
                    index = i;
                }
            }
        }
        return index;
    }

    private RecipeTypes getMatchingPattern() {
        ForgeroToolPart part = ((ToolPartItem) getOutput().getItem()).getPart();
        return part.getRecipeType();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory inventory) {
        DefaultedList<ItemStack> defaultList = super.getRemainder(inventory);
        int patternIndex = containsPattern(inventory);
        defaultList.set(patternIndex, inventory.getStack(patternIndex).copy());
        return defaultList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolPartRecipeSerializer.INSTANCE;
    }

    public static class ToolPartRecipeSerializer extends ShapelessRecipe.Serializer implements ForgeroRecipeSerializer {
        public static final ToolPartRecipeSerializer INSTANCE = new ToolPartRecipeSerializer();


        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public ToolPartRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new ToolPartRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public ToolPartRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new ToolPartRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_PART_RECIPE.getName());
        }
    }
}
