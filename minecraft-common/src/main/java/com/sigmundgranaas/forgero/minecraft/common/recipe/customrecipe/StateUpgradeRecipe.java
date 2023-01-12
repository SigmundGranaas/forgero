package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Set;

public class StateUpgradeRecipe extends SmithingRecipe {
    public StateUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (inventory.containsAny(Set.of(Items.AIR))) {
            return false;
        }
        if (super.matches(inventory, world)) {
            var originStateOpt = StateConverter.of(inventory.getStack(0))
                    .filter(Composite.class::isInstance)
                    .map(Composite.class::cast);
            var upgradeOpt = StateConverter.of(inventory.getStack(1));
            if (originStateOpt.isPresent() && upgradeOpt.isPresent()) {
                return originStateOpt.get().canUpgrade(upgradeOpt.get());
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
            output.getOrCreateNbt().put(NbtConstants.FORGERO_IDENTIFIER, CompoundEncoder.ENCODER.encode(upgraded));
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
            return new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_UPGRADE_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
