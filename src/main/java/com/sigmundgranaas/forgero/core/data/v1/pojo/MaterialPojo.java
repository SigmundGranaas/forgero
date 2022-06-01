package com.sigmundgranaas.forgero.core.data.v1.pojo;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;

import java.util.List;

public class MaterialPojo extends ForgeroDataResource {
    public MaterialType type;
    public IngredientPojo ingredient;
    public PalettePojo palette;
    public Primary primary;
    public Secondary secondary;

    public static MaterialPojo createDefaultMaterialPOJO() {
        MaterialPojo pojo = new MaterialPojo();
        pojo.name = "Default";
        pojo.type = MaterialType.METAL;
        pojo.properties = new PropertyPojo();
        PalettePojo palette = new PalettePojo();
        palette.include = List.of();
        palette.exclude = List.of();
        pojo.palette = palette;
        pojo.primary = new Primary();
        pojo.secondary = new Secondary();

        IngredientPojo ingredient = new IngredientPojo();
        ingredient.item = "ingredient";
        pojo.ingredient = ingredient;
        return pojo;
    }

    public static class Primary {
        public PropertyPojo properties;
    }

    public static class Secondary {
        public PropertyPojo properties;
    }
}
