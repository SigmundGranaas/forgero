package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ToolRecipe extends ShapedRecipe {
    public ToolRecipe(Identifier identifier, String string, int width, int height, DefaultedList<Ingredient> defaultedList, ItemStack itemStack) {
        super(identifier, string, width, height, defaultedList, itemStack);
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_RECIPE.getName());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return new ToolRecipeSerializer();
    }

    public static class ToolRecipeSerializer extends ShapedRecipe.Serializer, implements ForgeroRecipeSerializerTypes {
        @Override
        public ToolRecipe read(Identifier identifier, JsonObject jsonObject) {
            return (ToolRecipe) super.read(identifier, jsonObject);
        }

        @Override
        public ToolRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return (ToolRecipe) super.read(identifier, packetByteBuf);
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_RECIPE.getName());
        }
    }

}