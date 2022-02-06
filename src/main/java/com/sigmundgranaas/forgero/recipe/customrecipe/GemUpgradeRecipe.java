package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class GemUpgradeRecipe extends SmithingRecipe {
    private final Ingredient base;
    private final Ingredient addition;

    public GemUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
        Identifier id = recipe.getId();
        this.base = recipe.getBase();
        this.addition = recipe.getAddition();
    }

    public static Optional<Gem> getGem(Inventory inventory) {
        Gem baseGem;
        Gem additionGem;
        ItemStack base = inventory.getStack(0);
        ItemStack addition = inventory.getStack(1);
        if (base.hasNbt() && base.getOrCreateNbt().contains(NBTFactory.GEM_NBT_IDENTIFIER)) {
            baseGem = NBTFactory.INSTANCE.createGemFromNbt(base.getNbt());
        } else {
            baseGem = ((GemItem) base.getItem()).getGem();
        }

        if (addition.hasNbt() && addition.getOrCreateNbt().contains(NBTFactory.GEM_NBT_IDENTIFIER)) {
            additionGem = NBTFactory.INSTANCE.createGemFromNbt(addition.getNbt());
        } else {
            additionGem = ((GemItem) addition.getItem()).getGem();
        }

        return baseGem.upgradeGem(additionGem);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (super.matches(inventory, world)) {
            return getGem(inventory).isPresent();
        }
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        Gem resultingGem = getGem(inventory).orElse(((GemItem) getOutput().getItem()).getGem());

        ItemStack output = getOutput().copy();
        NBTFactory.INSTANCE.createNBTFromGem(resultingGem, output.getOrCreateNbt());
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
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.GEM_UPGRADE_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
