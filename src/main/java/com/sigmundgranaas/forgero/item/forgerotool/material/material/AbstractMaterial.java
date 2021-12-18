package com.sigmundgranaas.forgero.item.forgerotool.material.material;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AbstractMaterial implements Material {
    protected String name;
    protected int rarity;
    protected int durability;
    protected int weight;
    protected List<Identifier> paletteIdentifiers;
    protected List<Identifier> paletteExclusionIdentifiers;
    protected List<String> properties;
    protected MaterialType type;

    public AbstractMaterial(MaterialPOJO material) {
        this.name = material.name;
        this.rarity = material.rarity;
        this.durability = material.durability;
        this.weight = material.weight;
        this.paletteIdentifiers = material.palette.include;
        this.paletteExclusionIdentifiers = material.palette.exclude;
        this.properties = material.properties;
        this.type = material.type;
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
    public @NotNull List<Identifier> getPaletteIdentifiers() {
        return paletteIdentifiers;
    }

    @Override
    public @NotNull List<Identifier> getPaletteExclusionIdentifiers() {
        return paletteExclusionIdentifiers;
    }
}
