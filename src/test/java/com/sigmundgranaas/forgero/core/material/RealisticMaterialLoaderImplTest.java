package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.material.implementation.RealisticMaterialLoader;
import org.junit.jupiter.api.Test;

class RealisticMaterialLoaderImplTest {

    @Test
    void loadAllMaterials() {
        MaterialLoader loader = new RealisticMaterialLoader("/config/forgero/materials/realistic/realistic_materials.json");
        var materials = loader.getMaterials();
        assert (materials.size() > 0);
    }


    @Test
    void checkPojoCreation() {
        MaterialLoader loader = new RealisticMaterialLoader("/config/wrongMaterials.json");
    }
}