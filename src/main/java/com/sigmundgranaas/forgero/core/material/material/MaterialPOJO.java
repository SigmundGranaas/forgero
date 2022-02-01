package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.MaterialType;

import java.util.List;

public abstract class MaterialPOJO {
    public String name;
    public int rarity;
    public MaterialType type;
    public int durability;
    public List<String> properties;
    public Ingredient ingredient;
    public Palette palette;
    public Primary primary;
    public Secondary secondary;

    public static class Palette {
        public List<String> include;
        public List<String> exclude;
    }

    public static class Ingredient {
        public String item;
    }

    public abstract static class Primary {

    }

    public abstract static class Secondary {

    }
}
