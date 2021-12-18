package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.sigmundgranaas.forgero.exception.NoMaterialsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialLoaderTest {

    @Test
    void loadAllMaterials() {
        MaterialLoader loader = new MaterialLoaderImpl("/config/materials.json");
        var materials = loader.getMaterials();
        assert (materials.size() > 0);
    }

    @Test
    void throwExceptionOnWrongPath() {
        MaterialLoader loader = new MaterialLoaderImpl("/config/wrongMaterials.json");
        Assertions.assertThrows(NoMaterialsException.class, loader::getMaterials);
    }
}