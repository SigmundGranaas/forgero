package com.sigmundgranaas.forgero.core.gem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroGemTest {

    public static ForgeroGemTestClass createDefaultGem() {
        return new ForgeroGemTestClass(1, "test_gem");
    }

    @Test
    void getIdentifier() {
        var forgeroGem = createDefaultGem();
        Assertions.assertEquals("test_gem", forgeroGem.getIdentifier());
    }

    @Test
    void getLevel() {
        var forgeroGem = createDefaultGem();
        Assertions.assertEquals(1, forgeroGem.getLevel());
    }

    @Test
    void gemCannotBeCreatedWithLevelUnder1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ForgeroGemTestClass(0, "test"));
    }

    @Test
    void getDurabilityFromLevel1Gem() {
        DurabilityGem durabilityGem = createDefaultGem();
        Assertions.assertEquals(3, durabilityGem.applyDurability(1));
    }

    @Test
    void getDurabilityFromLevel9Gem() {
        DurabilityGem durabilityGem = new ForgeroGemTestClass(9, "test_gem");
        Assertions.assertEquals(19, durabilityGem.applyDurability(1));
    }

    private static class ForgeroGemTestClass extends ForgeroGem implements DurabilityGem {

        public ForgeroGemTestClass(int gemLevel, String identifier) {
            super(gemLevel, identifier);
        }

        @Override
        public ForgeroGem createNewGem(int level, String Identifier) {
            return new ForgeroGemTestClass(level, Identifier);
        }

        @Override
        public boolean equals(Gem newGem) {
            return (newGem instanceof ForgeroGemTestClass && newGem.getLevel() == getLevel());
        }

        @Override
        public int applyDurability(int currentDurability) {
            return currentDurability + 2 * getLevel();
        }

        @Override
        public GemTypes getType() {
            return GemTypes.DURABILITY;
        }
    }
}