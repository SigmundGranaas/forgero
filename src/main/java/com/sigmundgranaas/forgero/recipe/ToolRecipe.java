package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class ToolRecipe extends ShapedRecipe {

    public ToolRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolRecipeSerializer.INSTANCE;
    }

    public static class ToolRecipeSerializer extends ShapedRecipe.Serializer implements ForgeroRecipeSerializer {
        public static ToolRecipeSerializer INSTANCE = new ToolRecipeSerializer();

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public ToolRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new ToolRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public ToolRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new ToolRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_RECIPE.getName());
        }
    }
}