package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import com.sigmundgranaas.forgero.state.LeveledState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder.ENCODER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class GemUpgradeRecipe extends SmithingRecipe {
    public GemUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (super.matches(inventory, world)) {
            var base = base(inventory);
            var addition = addition(inventory);
            if (base.isPresent() && addition.isPresent()) {
                return base.get().level() == addition.get().level();

            }
        }
        return false;
    }

    private Optional<LeveledState> base(Inventory inventory) {
        var target = StateConverter.of(inventory.getStack(0));
        return target.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
    }

    private Optional<LeveledState> addition(Inventory inventory) {
        var addition = StateConverter.of(inventory.getStack(1));
        return addition.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        var base = base(inventory);
        var output = getOutput().copy();
        if (base.isPresent()) {
            var newBase = base.get().levelUp();
            var nbt = ENCODER.encode(newBase);
            output.getOrCreateNbt().put(FORGERO_IDENTIFIER, nbt);
        }
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer extends SmithingRecipe.Serializer implements ForgeroRecipeSerializer {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new GemUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, jsonObject));
        }

        @Override
        public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new GemUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.GEM_UPGRADE_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
