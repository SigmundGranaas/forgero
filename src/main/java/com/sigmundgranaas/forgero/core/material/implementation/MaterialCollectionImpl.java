package com.sigmundgranaas.forgero.core.material.implementation;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
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
 */
public class MaterialCollectionImpl implements MaterialCollection {
    private final Map<String, ForgeroMaterial> materials;

    public MaterialCollectionImpl(Map<String, ForgeroMaterial> materials) {
        this.materials = materials;
    }

    @Override
    public @NotNull
    Map<String, ForgeroMaterial> getMaterialsAsMap() {
        return materials;
    }

    @Override
    public @NotNull
    List<ForgeroMaterial> getMaterialsAsList() {
        return new ArrayList<>(materials.values());
    }

    @Override
    public @NotNull
    List<PrimaryMaterial> getPrimaryMaterialsAsList() {
        return materials.values().stream().filter(material -> material instanceof PrimaryMaterial).map(material -> (PrimaryMaterial) material).collect(Collectors.toList());
    }

    @Override
    public @NotNull
    List<SecondaryMaterial> getSecondaryMaterialsAsList() {
        return materials.values().stream().filter(material -> material instanceof SecondaryMaterial && !(material instanceof EmptySecondaryMaterial)).map(material -> (SecondaryMaterial) material).collect(Collectors.toList());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroToolIdentifier identifier) {
        return materials.get(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroToolPartIdentifier identifier) {
        return materials.get(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull
    ForgeroMaterial getMaterial(ForgeroMaterialIdentifier identifier) {
        return materials.get(identifier.getName());
    }
}
