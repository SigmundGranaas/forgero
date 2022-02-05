package com.sigmundgranaas.forgero.core.gem.gems;

import com.sigmundgranaas.forgero.core.gem.Gem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class AdditiveAttackDamageGemTest {

    @Test
    void testAdditiveDamageLevel1() {
        AdditiveAttackDamageGemImpl gem = new AdditiveAttackDamageGemImpl(1, "additive");
        Assertions.assertEquals(2, gem.applyAttackDamage(1));
    }

    @Test
    void testAdditiveDamageLevel9() {
        AdditiveAttackDamageGemImpl gem = new AdditiveAttackDamageGemImpl(9, "additive");
        Assertions.assertEquals(10, gem.applyAttackDamage(1));
    }

    @Test
    void upgradeGem() {
        AdditiveAttackDamageGemImpl gem1 = new AdditiveAttackDamageGemImpl(9, "additive");
        AdditiveAttackDamageGemImpl gem2 = new AdditiveAttackDamageGemImpl(9, "additive");
        Optional<Gem> gem3 = gem1.upgradeGem(gem2);
        if (gem3.isPresent() && gem3.get() instanceof AdditiveAttackDamageGemImpl) {
            Assertions.assertTrue(gem1.applyAttackDamage(1) < ((AdditiveAttackDamageGemImpl) gem3.get()).applyAttackDamage(1));
        } else {
            Assertions.fail();
        }
    }
}