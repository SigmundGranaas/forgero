package com.sigmundgranaas.forgero.core.material.material;

import com.google.gson.JsonObject;
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


    public static MaterialPOJO createDefualtMaterialPOJO() {
        MaterialPOJO pojo = new MaterialPOJO();
        pojo.name = "Default";
        pojo.rarity = 1;
        pojo.type = MaterialType.METAL;
        pojo.durability = 1;
        pojo.properties = List.of();
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
        return pojo;
    }

    public static class Palette {
        public List<Identifier> include;
        public List<Identifier> exclude;
    }

    public static class Primary {
        public int stiffness;
        public int sharpness;
        public int enchantability;
        public int flexibility;
        public JsonObject repairIngredient;
    }

    public static class Secondary {
        public int luck;
        public int grip;
    }

}
