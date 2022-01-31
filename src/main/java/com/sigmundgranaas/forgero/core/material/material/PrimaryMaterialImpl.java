package com.sigmundgranaas.forgero.core.material.material;

import com.google.gson.JsonObject;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrimaryMaterialImpl extends AbstractMaterial implements PrimaryMaterial {
    private final String ingredient;

    public PrimaryMaterialImpl(MaterialPOJO material) {
        super(material);
        ingredient = material.ingredient.item;
    }

    @Override
    public int getRarity() {
        return 0;
    }

    @Override
    public @NotNull
    List<Identifier> getPaletteIdentifiers() {
        return super.getPaletteIdentifiers();
    }

    @Override
    public @NotNull
    List<Identifier> getPaletteExclusionIdentifiers() {
        return super.getPaletteExclusionIdentifiers();
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public int getStiffNess() {
        return 0;
    }

    @Override
    public int getDurability() {
        return 0;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float getAttackDamage() {
        return 1f;
    }

    @Override
    public int getMiningLevel() {
        return 1;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        JsonObject json = new JsonObject();
        json.addProperty("item", ingredient);
        return Ingredient.fromJson(json);
    }

    @Override
    public int getSharpness() {
        return 0;
    }

    @Override
    public String getIngredientAsString() {
        return ingredient;
    }

    @Override
    public int getFlexibility() {
        return 0;
    }
}
