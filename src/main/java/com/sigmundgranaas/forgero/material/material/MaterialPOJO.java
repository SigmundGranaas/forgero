package com.sigmundgranaas.forgero.material.material;

import net.minecraft.util.Identifier;

import java.util.List;

public class MaterialPOJO {
    public String name;
    public int rarity;
    public MaterialType type;
    public int durability;
    public List<String> properties;
    public int weight;
    public Palette palette;
    public Primary primary;
    public Secondary secondary;

    public MaterialPOJO() {
        this.name = "Default";
        this.rarity = 1;
        this.type = MaterialType.METAL;
        this.durability = 1;
        this.properties = List.of();
        this.weight = 1;
        Palette palette = new Palette();
        palette.include = List.of();
        palette.exclude = List.of();
        this.palette = palette;
        Primary primary = new Primary();
        primary.enchantability = 1;
        primary.flexibility = 1;
        primary.sharpness = 1;
        primary.stiffness = 1;

        this.primary = primary;

        Secondary secondary = new Secondary();
        secondary.grip = 1;
        secondary.luck = 1;
        this.secondary = secondary;
    }


    public class Palette {
        public List<Identifier> include;
        public List<Identifier> exclude;
    }

    public class Primary {
        public int stiffness;
        public int sharpness;
        public int enchantability;
        public int flexibility;
    }

    public class Secondary {
        public int luck;
        public int grip;
    }

}
