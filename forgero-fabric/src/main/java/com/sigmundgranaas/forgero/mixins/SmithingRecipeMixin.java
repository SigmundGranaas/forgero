package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingRecipe.class)
public class SmithingRecipeMixin implements SmithingRecipeGetters {
    @Shadow
    @Final
    Ingredient base;

    @Shadow
    @Final
    Ingredient addition;
    @Shadow
    @Final
    ItemStack result;
    @Shadow
    @Final
    private Identifier id;

    @Override
    public Ingredient getBase() {
        return base;
    }

    @Override
    public Ingredient getAddition() {
        return addition;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
