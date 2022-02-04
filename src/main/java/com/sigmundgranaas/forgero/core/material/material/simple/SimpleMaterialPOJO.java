package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.MaterialType;

import java.util.List;

public class SimpleMaterialPOJO {
    public String name;
    public int rarity;
    public MaterialType type;
    public int durability;
    public List<String> properties;
    public Ingredient ingredient;
    public Palette palette;
    public Primary primary;
    public Secondary secondary;


    public static SimpleMaterialPOJO createDefaultMaterialPOJO() {
        SimpleMaterialPOJO pojo = new SimpleMaterialPOJO();
        pojo.name = "Default";
        pojo.rarity = 1;
        pojo.type = MaterialType.METAL;
        pojo.durability = 50;
        pojo.properties = List.of();
        Palette palette = new Palette();
        palette.include = List.of();
        palette.exclude = List.of();
        pojo.palette = palette;
        Primary primary = new Primary();
        primary.miningLevel = 5;
        primary.attackSpeed = 5;
        primary.miningSpeed = 5;
        primary.attackDamage = 5;
        pojo.primary = primary;


        Secondary secondary = new Secondary();

        pojo.secondary = secondary;

        secondary.miningLevel = 0;
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
        public int miningLevel;
        public int miningSpeed;
        public int attackSpeed;
        public int attackDamage;
    }

    public static class Secondary {
        public int miningLevel;
        public float miningSpeedAddition;
        public float attackSpeedAddition;
        public float attackDamageAddition;
    }
}
