package com.sigmundgranaas.forgero.core.data.v1.pojo;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;

import java.util.List;

public class MaterialPOJO extends ForgeroDataResource {
    public MaterialType type;
    public Ingredient ingredient;
    public PalettePOJO palette;
    public Primary primary;
    public Secondary secondary;

    public static MaterialPOJO createDefaultMaterialPOJO() {
        MaterialPOJO pojo = new MaterialPOJO();
        pojo.name = "Default";
        pojo.type = MaterialType.METAL;
        pojo.properties = new PropertyPOJO();
        PalettePOJO palette = new PalettePOJO();
        palette.include = List.of();
        palette.exclude = List.of();
        pojo.palette = palette;
        pojo.primary = new Primary();
        pojo.secondary = new Secondary();

        Ingredient ingredient = new Ingredient();
        ingredient.item = "ingredient";
        pojo.ingredient = ingredient;
        return pojo;
    }

    public static class Ingredient {
        public String item;
    }

    public static class Primary {
        public PropertyPOJO properties;
    }

    public static class Secondary {
        public PropertyPOJO properties;
    }
}
