package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import com.sigmundgranaas.forgero.state.Composite;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder.COMPOSITE_ENCODER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class StateUpgradeRecipe extends SmithingRecipe {
    public StateUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (inventory.containsAny(ItemStack::isEmpty)) {
            return false;
        }
        if (super.matches(inventory, world)) {
            var originStateOpt = StateConverter.of(inventory.getStack(0));
            var upgradeOpt = StateConverter.of(inventory.getStack(1));
            if (originStateOpt.isPresent() && upgradeOpt.isPresent() && originStateOpt.get() instanceof Composite composite) {
                return composite.canUpgrade(upgradeOpt.get());
            }
        }
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        var originStateOpt = StateConverter.of(inventory.getStack(0));
        var upgradeOpt = StateConverter.of(inventory.getStack(1));
        if (originStateOpt.isPresent() && upgradeOpt.isPresent() && originStateOpt.get() instanceof Composite state) {
            var upgraded = state.upgrade(upgradeOpt.get());
            var output = getOutput().copy();
            output.setNbt(inventory.getStack(0).getOrCreateNbt().copy());
            output.getOrCreateNbt().put(FORGERO_IDENTIFIER, COMPOSITE_ENCODER.encode(upgraded));
            return output;
        }
        return inventory.getStack(0);
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
