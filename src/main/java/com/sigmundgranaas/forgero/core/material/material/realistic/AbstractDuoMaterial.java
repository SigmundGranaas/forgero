package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;


public abstract class AbstractDuoMaterial extends AbstractForgeroMaterial implements RealisticPrimaryMaterial, RealisticSecondaryMaterial {
    private final int stiffness;
    private final int sharpness;
    private final int enchantability;
    private final int flexibility;
    private final int luck;
    private final int grip;
    private final String ingredient;

    public AbstractDuoMaterial(RealisticMaterialPOJO material) {
        super(material);
        RealisticMaterialPOJO.RealisticPrimaryMaterialPOJO primary = ((RealisticMaterialPOJO.RealisticPrimaryMaterialPOJO) material.primary);
        RealisticMaterialPOJO.RealisticSecondaryMaterialPOJO secondary = (RealisticMaterialPOJO.RealisticSecondaryMaterialPOJO) material.secondary;
        this.stiffness = primary.stiffness;
        this.sharpness = primary.sharpness;
        this.enchantability = primary.enchantability;
        this.flexibility = primary.flexibility;
        this.luck = secondary.luck;
        this.grip = secondary.grip;
        this.ingredient = material.ingredient.item;
    }

    public int getStiffness() {
        return stiffness;
    }

    @Override
    public int getSharpness() {
        return sharpness;
    }

    @Override
    public String getIngredient() {
        return ingredient;
    }

    @Override
    public int getLuck() {
        return luck;
    }

    @Override
    public int getGrip() {
        return grip;
    }
}
