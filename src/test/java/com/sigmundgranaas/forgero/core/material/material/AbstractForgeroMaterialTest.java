package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractForgeroMaterialTest {
    AbstractForgeroMaterial defaultTestMaterial;

    @BeforeEach
    void setUpDefaultMaterial() {
        defaultTestMaterial = new AbstractForgeroMaterialTestClass(RealisticMaterialPOJO.createDefaultMaterialPOJO());
    }

    @Test
    void getName() {
        Assertions.assertNotNull(defaultTestMaterial.getName());
        Assertions.assertEquals("default", defaultTestMaterial.getName());
    }

    @Test
    void getProperties() {
        Assertions.assertNotNull(defaultTestMaterial.getProperties());
    }
    

    @Test
    void abstractMaterialsFromPojoBehaveTheSame() {
        AbstractForgeroMaterial realisticMaterial = new AbstractForgeroMaterialTestClass(RealisticMaterialPOJO.createDefaultMaterialPOJO());
        AbstractForgeroMaterial simpleMaterial = new AbstractForgeroMaterialTestClass(SimpleMaterialPOJO.createDefaultMaterialPOJO());

        Assertions.assertEquals(realisticMaterial.getName(), simpleMaterial.getName());
    }

    private static class AbstractForgeroMaterialTestClass extends AbstractForgeroMaterial {
        public AbstractForgeroMaterialTestClass(RealisticMaterialPOJO material) {
            super(material);
        }

        public AbstractForgeroMaterialTestClass(SimpleMaterialPOJO material) {
            super(material);
        }
    }
}