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


    public static class Palette {
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
