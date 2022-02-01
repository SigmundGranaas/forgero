package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.implementation.RealisticMaterialLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RealisticMaterialLoaderImplTest {

    @Test
    void loadAllMaterials() {
        MaterialLoader loader = new RealisticMaterialLoader("/config/materials.json");
        var materials = loader.getMaterials();
        assert (materials.size() > 0);
    }

    @Test
    void throwExceptionOnWrongPath() {
        MaterialLoader loader = new RealisticMaterialLoader("/config/wrongMaterials.json");
        Assertions.assertThrows(NoMaterialsException.class, loader::getMaterials);
    }

    @Test
    void checkPojoCreation() {
        MaterialLoader loader = new RealisticMaterialLoader("/config/wrongMaterials.json");
    }
}