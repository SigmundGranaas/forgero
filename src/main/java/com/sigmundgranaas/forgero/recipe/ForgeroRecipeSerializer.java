package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.item.implementation.ToolPartItemImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;


public class ForgeroRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final ToolPartItemImpl toolpart;
    private final Function<Identifier, ToolPartItemImpl, T> factory;

    public ForgeroRecipeSerializer(ToolPartItemImpl toolpart, Function<Identifier, ToolPartItemImpl, T> factory) {
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
