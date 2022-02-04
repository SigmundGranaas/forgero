package com.sigmundgranaas.forgero.item.adapter;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHeadStrategy;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ToolMaterialAdapter implements ToolMaterial {
    ToolPartHeadStrategy strategy;

    public ToolMaterialAdapter(PrimaryMaterial material) {
        this.strategy = ToolPartStrategyFactory.createToolPartHeadStrategy(ForgeroToolTypes.PICKAXE, material);
    }

    @Override
    public int getDurability() {
        return strategy.getDurability();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return strategy.getMiningSpeedMultiplier();
    }

    @Override
    public float getAttackDamage() {
        return strategy.getAttackDamage();
    }

    @Override
    public int getMiningLevel() {
        return strategy.getMiningLevel();
    }

    @Override
    public int getEnchantability() {
        return 10;
    }

    @Override
    public Ingredient getRepairIngredient() {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", strategy.getMaterial().getIngredient());
        return Ingredient.fromJson(ingredient);
    }
}
