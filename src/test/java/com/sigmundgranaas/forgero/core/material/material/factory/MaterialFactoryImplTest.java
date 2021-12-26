package com.sigmundgranaas.forgero.core.material.material.factory;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialFactoryImplTest {


    @Test
    void createMaterial() {
        MaterialFactoryImpl factory = new MaterialFactoryImpl();
        ForgeroMaterial material = factory.createMaterial(MaterialPOJO.createDefualtMaterialPOJO());

        Assertions.assertEquals("default", material.getName());
    }
}