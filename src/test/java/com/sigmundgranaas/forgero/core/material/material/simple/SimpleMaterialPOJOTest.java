package com.sigmundgranaas.forgero.core.material.material.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleMaterialPOJOTest {
    SimpleMaterialPOJO defaultTestMaterial;

    @BeforeEach
    void setUpDefaultMaterial() {
        defaultTestMaterial = SimpleMaterialPOJO.createDefaultMaterialPOJO();
    }

    @Test
    void defaultMiningLevelIs5() {
        Assertions.assertEquals(5, defaultTestMaterial.primary.miningLevel);
    }

    @Test
    void defaultMiningSpeedIs5() {
        Assertions.assertEquals(5, defaultTestMaterial.primary.miningSpeed);
    }

    @Test
    void defaultAttackSpeedIs5() {
        Assertions.assertEquals(5, defaultTestMaterial.primary.attackSpeed);
    }

    @Test
    void defaultDamageIs5() {
        Assertions.assertEquals(5, defaultTestMaterial.primary.attackDamage);
    }

    @Test
    void defaultDurabilityIs50() {
        Assertions.assertEquals(50, defaultTestMaterial.durability);
    }
}