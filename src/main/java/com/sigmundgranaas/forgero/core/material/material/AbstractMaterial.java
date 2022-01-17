package com.sigmundgranaas.forgero.core.material.material;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AbstractMaterial implements ForgeroMaterial {
    protected String name;
    protected int rarity;
    protected int durability;
    protected int weight;
    protected List<String> paletteIdentifiers;
    protected List<String> paletteExclusionIdentifiers;
    protected List<String> properties;
    protected MaterialType type;
    protected String ingredient;

    public AbstractMaterial(MaterialPOJO material) {
        this.name = material.name.toLowerCase(Locale.ROOT);
        this.rarity = material.rarity;
        this.durability = material.durability;
        this.weight = material.weight;
        this.paletteIdentifiers = material.palette.include;
        this.paletteExclusionIdentifiers = material.palette.exclude;
        this.properties = material.properties;
        this.type = material.type;
        this.ingredient = material.ingredient.item;
    }

    @Override
    public int getRarity() {
        return rarity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public MaterialType getType() {
        return type;
    }

    @Override
    public List<String> getProperties() {
        return properties;
    }

    @Override
    public String getIngredientAsString() {
        return ingredient;
    }

    @Override
    public @NotNull
    List<Identifier> getPaletteIdentifiers() {
        return paletteIdentifiers.stream().map(Identifier::new).collect(Collectors.toList());
    }

    @Override
    public @NotNull
    List<Identifier> getPaletteExclusionIdentifiers() {
        return paletteExclusionIdentifiers.stream().map(Identifier::new).collect(Collectors.toList());

    }
}
