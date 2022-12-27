package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;

public class SchematicPartRecipe extends ShapelessRecipe {

    public SchematicPartRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SchematicPartRecipeSerializer.INSTANCE;
    }

    public static class SchematicPartRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
        public static final SchematicPartRecipeSerializer INSTANCE = new SchematicPartRecipeSerializer();

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public SchematicPartRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new SchematicPartRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public SchematicPartRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new SchematicPartRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.NAMESPACE, RecipeTypes.SCHEMATIC_PART_CRAFTING.getName());
        }
    }
}