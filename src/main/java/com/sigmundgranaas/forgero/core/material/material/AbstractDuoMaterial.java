package com.sigmundgranaas.forgero.core.material.material;

public abstract class AbstractDuoMaterial extends AbstractMaterial implements PrimaryMaterial, SecondaryMaterial {
    private final int stiffness;
    private final int sharpness;
    private final int enchantability;
    private final int flexibility;
    private final int luck;
    private final int grip;

    public AbstractDuoMaterial(MaterialPOJO material) {
        super(material);
        this.stiffness = material.primary.stiffness;
        this.sharpness = material.primary.sharpness;
        this.enchantability = material.primary.enchantability;
        this.flexibility = material.primary.flexibility;
        this.luck = material.secondary.luck;
        this.grip = material.secondary.grip;
    }

    public int getStiffness() {
        return stiffness;
    }

    @Override
    public int getSharpness() {
        return sharpness;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public int getFlexibility() {
        return flexibility;
    }


    @Override
    public int getLuck() {
        return luck;
    }

    @Override
    public int getStiffNess() {
        return 0;
    }

    @Override
    public int getGrip() {
        return grip;
    }


}
