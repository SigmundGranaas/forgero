package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.Material;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A singleton implementation of MaterialCollection
 * Will load material when creating
 */
public class MaterialCollectionImpl implements MaterialCollection {
    private static MaterialCollectionImpl INSTANCE;
    private final MaterialLoader loader;
    private Map<String, Material> materials;

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

    private Map<String, Material> getMaterials() {
        if (materials == null) {
            loadMaterials();
        }
        return materials;
    }

    @Override
    public @NotNull Map<String, Material> getMaterialsAsMap() {
        return getMaterials();
    }

    @Override
    public @NotNull List<Material> getMaterialsAsList() {
        return new ArrayList<Material>(getMaterials().values());
    }

    @Override
    public @NotNull List<PrimaryMaterial> getPrimaryMaterialsAsList() {
        return null;
    }

    @Override
    public @NotNull List<SecondaryMaterial> getSecondaryMaterialsAsList() {
        return null;
    }

    @Override
    public @NotNull Material getMaterial(ForgeroToolIdentifier identifier) {
        return null;
    }

    @Override
    public @NotNull Material getMaterial(ForgeroToolPartIdentifier identifier) {
        return null;
    }

    @Override
    public @NotNull Material getMaterial(ForgeroMaterialIdentifier identifier) {
        return null;
    }
}
