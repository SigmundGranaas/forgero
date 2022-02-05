package com.sigmundgranaas.forgero.core.gem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmptyGemTest {

    @Test
    void applyAttackDamage() {
        Assertions.assertEquals(1, EmptyGem.createEmptyGem().applyAttackDamage(1));
    }

    @Test
    void applyDurability() {
        Assertions.assertEquals(1, EmptyGem.createEmptyGem().applyDurability(1));
    }

    @Test
    void applyMiningSpeed() {
        Assertions.assertEquals(1, EmptyGem.createEmptyGem().applyAttackSpeed(1));
    }
}