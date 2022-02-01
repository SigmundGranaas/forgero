package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.material.material.MaterialPOJO;

import java.util.List;

public class RealisticMaterialPOJO extends MaterialPOJO {
    public int weight;

    public static RealisticMaterialPOJO createDefaultMaterialPOJO() {
        RealisticMaterialPOJO pojo = new RealisticMaterialPOJO();
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
        RealisticPrimaryMaterialPOJO primary = new RealisticPrimaryMaterialPOJO();
        primary.enchantability = 1;
        primary.flexibility = 1;
        primary.sharpness = 1;
        primary.stiffness = 1;

        pojo.primary = primary;

        RealisticSecondaryMaterialPOJO secondary = new RealisticSecondaryMaterialPOJO();
        secondary.grip = 1;
        secondary.luck = 1;
        pojo.secondary = secondary;

        Ingredient ingredient = new Ingredient();
        ingredient.item = "ingredient";
        pojo.ingredient = ingredient;
        return pojo;
    }

    public static class RealisticPrimaryMaterialPOJO extends MaterialPOJO.Primary {
        public int stiffness;
        public int sharpness;
        public int enchantability;
        public int flexibility;
    }

    public static class RealisticSecondaryMaterialPOJO extends MaterialPOJO.Secondary {
        public int luck;
        public int grip;
    }

}
