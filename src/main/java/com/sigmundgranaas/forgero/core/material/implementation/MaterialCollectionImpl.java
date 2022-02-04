package com.sigmundgranaas.forgero.core.material.implementation;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A singleton implementation of MaterialCollection
 * Will load material when creating
 */
public class MaterialCollectionImpl implements MaterialCollection {
    private static MaterialCollectionImpl INSTANCE;
    private final MaterialLoader loader;
    private Map<String, ForgeroMaterial> materials;

    public MaterialCollectionImpl(MaterialLoader loader) {
        this.loader = loader;
    }

    public static MaterialCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        }
        return INSTANCE;
    }

    private void loadMaterials() {
        materials = loader.getMaterials();
    }

    private Map<String, ForgeroMaterial> getMaterials() {
        if (materials == null) {
            loadMaterials();
        }
        return materials;
    }

    @Override
    public @NotNull
    Map<String, ForgeroMaterial> getMaterialsAsMap() {
        return getMaterials();
    }

    @Override
    public @NotNull
    List<ForgeroMaterial> getMaterialsAsList() {
        return new ArrayList<>(getMaterials().values());
    }

    @Override
    public @NotNull
    List<PrimaryMaterial> getPrimaryMaterialsAsList() {
        return getMaterials().values().stream().filter(material -> material instanceof PrimaryMaterial).map(material -> (PrimaryMaterial) material).collect(Collectors.toList());
    }

    @Override
    public @NotNull
    List<SecondaryMaterial> getSecondaryMaterialsAsList() {
        return getMaterials().values().stream().filter(material -> material instanceof SecondaryMaterial && !(material instanceof EmptySecondaryMaterial)).map(material -> (SecondaryMaterial) material).collect(Collectors.toList());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroToolIdentifier identifier) {
        return getMaterials().get(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroToolPartIdentifier identifier) {
        return getMaterials().get(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroMaterialIdentifier identifier) {
        return getMaterials().get(identifier.getName());
    }
}
