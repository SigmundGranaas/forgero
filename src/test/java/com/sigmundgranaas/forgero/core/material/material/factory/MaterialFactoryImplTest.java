package com.sigmundgranaas.forgero.core.material.material.factory;

import com.sigmundgranaas.forgero.core.data.factory.MaterialFactoryImpl;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialFactoryImplTest {
    public static final ForgeroMaterial testMaterial = new MaterialFactoryImpl().createMaterial(RealisticMaterialPOJO.createDefaultMaterialPOJO());

    @Test
    void materialName() {
        Assertions.assertEquals("default", testMaterial.getName());
    }

    @Test
    void rarity() {
        Assertions.assertEquals(1, testMaterial.getRarity());
    }

    @Test
    void materialType() {
        Assertions.assertEquals(MaterialType.METAL, testMaterial.getType());
    }


    @Test
    void properties() {
        Assertions.assertEquals(0, testMaterial.getProperties().size());
    }


    @Test
    void ingredient() {
        Assertions.assertEquals("ingredient", testMaterial.getIngredient());
    }
}