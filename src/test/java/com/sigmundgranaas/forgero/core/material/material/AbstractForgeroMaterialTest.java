package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractForgeroMaterialTest {
    AbstractForgeroMaterial defaultTestMaterial;

    @BeforeEach
    void setUpDefaultMaterial() {
        defaultTestMaterial = new AbstractForgeroMaterialTestClass(RealisticMaterialPOJO.createDefaultMaterialPOJO());
    }

    @Test
    void getProperties() {
    }

    @Test
    void getPaletteIdentifiers() {
    }

    @Test
    void getPaletteExclusionIdentifiers() {
    }

    private static class AbstractForgeroMaterialTestClass extends AbstractForgeroMaterial {
        public AbstractForgeroMaterialTestClass(RealisticMaterialPOJO material) {
            super(material);
        }
    }
}