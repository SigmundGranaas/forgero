package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

import java.util.List;

public class RealisticMaterialPOJO {
    public int weight;
    public Primary primary;
    public Secondary secondary;

    public String name;
    public int rarity;
    public MaterialType type;
    public int durability;
    public PropertyPOJO properties;
    public Ingredient ingredient;
    public Palette palette;

    public static RealisticMaterialPOJO createDefaultMaterialPOJO() {
        RealisticMaterialPOJO pojo = new RealisticMaterialPOJO();
        pojo.name = "Default";
        pojo.rarity = 1;
        pojo.type = MaterialType.METAL;
        pojo.durability = 1;
        pojo.weight = 1;
        Palette palette = new Palette();
        palette.include = List.of();
        palette.exclude = List.of();
        pojo.palette = palette;
        Primary primary = new Primary();
        primary.enchantability = 1;
        primary.flexibility = 1;
        primary.sharpness = 1;
        primary.stiffness = 1;

        pojo.primary = primary;

        Secondary secondary = new Secondary();
        secondary.grip = 1;
        secondary.luck = 1;
        pojo.secondary = secondary;

        Ingredient ingredient = new Ingredient();
        ingredient.item = "ingredient";
        pojo.ingredient = ingredient;
        return pojo;
    }

    public static class Palette {
        public List<String> include;
        public List<String> exclude;
    }

    public static class Ingredient {
        public String item;
    }

    public static class Primary {
        public int stiffness;
        public int sharpness;
        public int enchantability;
        public int flexibility;
    }

    public static class Secondary {
        public int luck;
        public int grip;
    }
}
