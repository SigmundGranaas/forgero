package com.sigmundgranaas.forgero.item.forgerotool.material;

public class ForgeroPrimaryMaterial {
    private final String name;
    private final String type;
    private final int weight;
    private final int stiffness;
    private final int durability;
    private final int sharpness;
    private final int enchantability;
    private final int flexibility;
    private final int rarity;

    public ForgeroPrimaryMaterial(String name, String type, int weight, int stiffness, int durability, int sharpness, int enchantability, int flexibility, int rarity) {
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.stiffness = stiffness;
        this.durability = durability;
        this.sharpness = sharpness;
        this.enchantability = enchantability;
        this.flexibility = flexibility;
        this.rarity = rarity;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public int getStiffness() {
        return stiffness;
    }

    public int getDurability() {
        return durability;
    }

    public int getSharpness() {
        return sharpness;
    }

    public int getEnchantability() {
        return enchantability;
    }

    public int getFlexibility() {
        return flexibility;
    }

    public int getRarity() {
        return rarity;
    }
}
