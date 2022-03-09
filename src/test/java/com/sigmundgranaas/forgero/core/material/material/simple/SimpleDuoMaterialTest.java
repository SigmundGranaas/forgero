package com.sigmundgranaas.forgero.core.material.material.simple;

import org.junit.jupiter.api.Test;

class SimpleDuoMaterialTest {
    @Test
    void SimpleDuoMaterialHasSameAttributesAsPOJO() {
        SimpleMaterialPOJO pojo = SimpleMaterialPOJO.createDefaultMaterialPOJO();
        SimpleDuoMaterial material = new SimpleDuoMaterial(pojo);

    }
}