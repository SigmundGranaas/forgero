package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class AbstractForgeroMaterial implements ForgeroMaterial {
    protected final String name;
    protected final int rarity;
    protected final int durability;
    protected final List<String> paletteIdentifiers;
    protected final List<String> paletteExclusionIdentifiers;
    protected final List<String> properties;
    protected final MaterialType type;
    protected final String ingredient;

    public AbstractForgeroMaterial(RealisticMaterialPOJO material) {
        this.name = material.name.toLowerCase(Locale.ROOT);
        this.rarity = material.rarity;
        this.durability = material.durability;
        this.paletteIdentifiers = material.palette.include;
        this.paletteExclusionIdentifiers = material.palette.exclude;
        this.properties = material.properties;
        this.type = material.type;
        this.ingredient = material.ingredient.item;
    }

    public AbstractForgeroMaterial(SimpleMaterialPOJO material) {
        this.name = material.name.toLowerCase(Locale.ROOT);
        this.rarity = material.rarity;
        this.durability = material.durability;
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
    public @NotNull String getName() {
        return name.toLowerCase();
    }

    @Override
    public int getDurability() {
        return durability;
    }


    @Override
    public MaterialType getType() {
        return type;
    }

    @Override
    public @NotNull List<String> getProperties() {
        return properties;
    }

    @Override
    public String getIngredient() {
        return ingredient;
    }

    @Override
    public @NotNull List<PaletteResourceIdentifier> getPaletteIdentifiers() {
        return paletteIdentifiers.stream().map(paletteIdentifiers -> new PaletteResourceIdentifier(new PaletteIdentifier(new ForgeroMaterialIdentifierImpl(this.name)), paletteIdentifiers)).collect(Collectors.toList());
    }

    @Override
    public @NotNull List<PaletteResourceIdentifier> getPaletteExclusionIdentifiers() {
        return paletteExclusionIdentifiers.stream().map(paletteIdentifiers -> new PaletteResourceIdentifier(new PaletteIdentifier(new ForgeroMaterialIdentifierImpl(this.name)), paletteIdentifiers)).collect(Collectors.toList());
    }
}
