package com.sigmundgranaas.forgero.item.adapter;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadStrategy;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ToolMaterialAdapter implements ToolMaterial {
    private final HeadStrategy strategy;
    private final HeadState state;

    public ToolMaterialAdapter(PrimaryMaterial material) {
        this.state = new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), ForgeroToolTypes.PICKAXE);
        this.strategy = state.createHeadStrategy();
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
        ingredient.addProperty("item", state.getPrimaryMaterial().getIngredient());
        return Ingredient.fromJson(ingredient);
    }
}
