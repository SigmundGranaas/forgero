package com.sigmundgranaas.forgero.item.forgerotool.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ForgeroToolWithBindingRecipeSerializer implements RecipeSerializer<ForgeroToolWithBindingRecipe> {
    public static final ForgeroToolWithBindingRecipeSerializer INSTANCE = new ForgeroToolWithBindingRecipeSerializer();
    // This will be the "type" field in the json
    public static final Identifier ID = new Identifier(Forgero.MOD_NAMESPACE, "tool_recipe_binding");

    private ForgeroToolWithBindingRecipeSerializer() {
    }

    @Override
    public ForgeroToolWithBindingRecipe read(Identifier id, JsonObject json) {
        ForgeroToolWithBindingRecipeSerializerJsonFormat recipeJson = new Gson().fromJson(json, ForgeroToolWithBindingRecipeSerializerJsonFormat.class);
        if (recipeJson.head == null || recipeJson.handle == null || recipeJson.binding == null || recipeJson.outputItem == null) {
            throw new JsonSyntaxException("A required attribute is missing!");
        }

        Ingredient head = Ingredient.fromJson(recipeJson.head);
        Ingredient binding = Ingredient.fromJson(recipeJson.binding);
        Ingredient handle = Ingredient.fromJson(recipeJson.handle);
        Item outPutItem = Registry.ITEM.getOrEmpty(new Identifier(recipeJson.outputItem)).orElseThrow(() -> new JsonSyntaxException("No such item " + recipeJson.outputItem));
        return new ForgeroToolWithBindingRecipe(head, handle, binding, outPutItem, id);
    }

    @Override
    public ForgeroToolWithBindingRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient head = Ingredient.fromPacket(buf);
        Ingredient handle = Ingredient.fromPacket(buf);
        Ingredient binding = Ingredient.fromPacket(buf);
        Item output = buf.readItemStack().getItem();
        return new ForgeroToolWithBindingRecipe(head, handle, binding, output, id);
    }

    @Override
    public void write(PacketByteBuf buf, ForgeroToolWithBindingRecipe recipe) {
        recipe.getHead().write(buf);
        recipe.getHandle().write(buf);
        recipe.getBinding().write(buf);
        buf.writeItemStack(new ItemStack(recipe.getItemOutput()));
    }
}

