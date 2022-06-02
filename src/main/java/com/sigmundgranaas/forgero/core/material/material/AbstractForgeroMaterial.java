package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.core.data.v1.pojo.IngredientPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public abstract class AbstractForgeroMaterial implements ForgeroMaterial {
    protected final String name;
    protected final int rarity;
    protected final List<String> paletteIdentifiers;
    protected final List<String> paletteExclusionIdentifiers;
    protected final List<Property> properties;
    protected final MaterialType type;
    protected final IngredientPojo ingredient;

    public AbstractForgeroMaterial(MaterialPojo material) {
        this.name = material.name.toLowerCase(Locale.ROOT);
        this.rarity = 0;
        this.paletteIdentifiers = material.palette.include;
        this.paletteExclusionIdentifiers = material.palette.exclude;
        this.properties = PropertyBuilder.createPropertyListFromPOJO(material.properties);

        this.type = material.type;
        this.ingredient = material.ingredient;
    }


    @Override
    public int getRarity() {
        return rarity;
    }

    @Override
    public @NotNull
    String getResourceName() {
        return name.toLowerCase();
    }

    @Override
    public MaterialType getType() {
        return type;
    }

    @Override
    public @NotNull
    List<Property> getProperties() {
        return properties;
    }

    @Override
    public IngredientPojo getIngredient() {
        return ingredient;
    }

    @Override
    public MaterialPojo toDataResource() {
        return new MaterialPojo();
    }
}
