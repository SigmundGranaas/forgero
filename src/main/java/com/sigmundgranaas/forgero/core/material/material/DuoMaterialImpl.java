package com.sigmundgranaas.forgero.core.material.material;

import com.google.gson.JsonObject;
import net.minecraft.recipe.Ingredient;

public class DuoMaterialImpl extends AbstractDuoMaterial {

    public DuoMaterialImpl(MaterialPOJO material) {
        super(material);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.fromJson(new JsonObject());
    }
}
