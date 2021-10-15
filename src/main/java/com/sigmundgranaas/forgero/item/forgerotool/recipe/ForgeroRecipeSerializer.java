package com.sigmundgranaas.forgero.item.forgerotool.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;


public class ForgeroRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final ForgeroToolPartItem toolpart;
    private final Function<Identifier, ForgeroToolPartItem, T> factory;

    public ForgeroRecipeSerializer(ForgeroToolPartItem toolpart, Function<Identifier, ForgeroToolPartItem, T> factory) {
        this.toolpart = toolpart;
        this.factory = factory;
    }

    @Override
    public T read(Identifier id, JsonObject json) {
        return this.factory.apply(id, toolpart);
    }

    @Override
    public T read(Identifier id, PacketByteBuf buf) {
        return this.factory.apply(id, toolpart);
    }

    @Override
    public void write(PacketByteBuf buf, T recipe) {

    }

    @FunctionalInterface
    public interface Function<A, B, T> {
        public T apply(A one, B two);
    }
}
