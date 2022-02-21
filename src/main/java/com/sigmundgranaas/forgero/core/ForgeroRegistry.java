package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import com.sigmundgranaas.forgero.core.util.ListPOJO;

import java.util.Collections;
import java.util.List;

public class ForgeroRegistry {
    private static ForgeroRegistry INSTANCE;
    private List<String> materials;
    private List<String> gems;

    private ForgeroRegistry() {
        this.materials = Collections.emptyList();
        this.gems = Collections.emptyList();
    }

    public static ForgeroRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroRegistry();
        }
        return INSTANCE;
    }

    public void registerDefaultMaterials() {
        JsonPOJOLoader
                .loadPOJO("/data/forgero/materials/materials.json", ListPOJO.class)
                .ifPresent(materialList -> materials = materialList.elements);
    }

    public void registerDefaultGems() {
        JsonPOJOLoader
                .loadPOJO("/data/forgero/gems/gems.json", ListPOJO.class)
                .ifPresent(gemList -> gems = gemList.elements);
    }

    public List<String> getMaterials() {
        return materials;
    }

    public List<String> getGems() {
        return gems;
    }

    boolean registerMaterial(String material) {
        return false;
    }

    public void initializeDefaults() {
        registerDefaultGems();
        registerDefaultMaterials();
    }
}
