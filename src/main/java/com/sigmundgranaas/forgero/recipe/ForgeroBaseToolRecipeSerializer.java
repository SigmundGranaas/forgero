package com.sigmundgranaas.forgero.recipe;

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

public class ForgeroBaseToolRecipeSerializer implements RecipeSerializer<ForgeroBaseToolRecipe> {
    public static final ForgeroBaseToolRecipeSerializer INSTANCE = new ForgeroBaseToolRecipeSerializer();
    // This will be the "type" field in the json
    public static final Identifier ID = new Identifier(Forgero.MOD_NAMESPACE, "base_tool_recipe");

    private ForgeroBaseToolRecipeSerializer() {
    }

    @Override
    public ForgeroBaseToolRecipe read(Identifier id, JsonObject json) {
        ForgeroBaseToolRecipeJsonFormat recipeJson = new Gson().fromJson(json, ForgeroBaseToolRecipeJsonFormat.class);
        if (recipeJson.head == null || recipeJson.handle == null || recipeJson.outputItem == null) {
            throw new JsonSyntaxException("A required attribute is missing!");
        }

        Ingredient head = Ingredient.fromJson(recipeJson.head);
        Ingredient handle = Ingredient.fromJson(recipeJson.handle);
        Item outPutItem = Registry.ITEM.getOrEmpty(new Identifier(recipeJson.outputItem)).orElseThrow(() -> new JsonSyntaxException("No such item " + recipeJson.outputItem));
        return new ForgeroBaseToolRecipe(head, handle, outPutItem, id);
    }

    @Override
    public ForgeroBaseToolRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient head = Ingredient.fromPacket(buf);
        Ingredient handle = Ingredient.fromPacket(buf);
        Item output = buf.readItemStack().getItem();
        return new ForgeroBaseToolRecipe(head, handle, output, id);
    }

    @Override
    public void write(PacketByteBuf buf, ForgeroBaseToolRecipe recipe) {
        recipe.getHead().write(buf);
        recipe.getHandle().write(buf);
        buf.writeItemStack(new ItemStack(recipe.getItemOutput()));
    }
}
