package com.sigmundgranaas.forgero.core.material.material.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleDuoMaterialTest {
    @Test
    void SimpleDuoMaterialHasSameAttributesAsPOJO() {
        SimpleMaterialPOJO pojo = SimpleMaterialPOJO.createDefaultMaterialPOJO();
        SimpleDuoMaterial material = new SimpleDuoMaterial(pojo);

        Assertions.assertEquals(pojo.primary.damage, material.getAttackDamage());
        Assertions.assertEquals(pojo.primary.attackSpeed, material.getAttackSpeed());
        Assertions.assertEquals(pojo.primary.miningLevel, material.getMiningLevel());
        Assertions.assertEquals(pojo.primary.miningSpeed, material.getMiningSpeed());
    }
}