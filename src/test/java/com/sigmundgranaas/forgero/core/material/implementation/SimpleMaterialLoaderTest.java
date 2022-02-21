package com.sigmundgranaas.forgero.core.material.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SimpleMaterialLoaderTest {

    @Test
    void loadMaterials() {
        Assertions.assertTrue(new SimpleMaterialLoader(List.of("oak")).getMaterials().size() > 0);
    }

    @Test
    void loadMaterial() {
    }

    @Test
    void getMaterials() {
    }
}