package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StateUpgradeRecipe extends SmithingRecipe {
    public StateUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (inventory.containsAny(ItemStack::isEmpty)) {
            return false;
        }
        return super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return getOutput().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer extends SmithingRecipe.Serializer implements ForgeroRecipeSerializer {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new StateUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, jsonObject));
        }

        @Override
        public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new StateUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.STATE_UPGRADE_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
