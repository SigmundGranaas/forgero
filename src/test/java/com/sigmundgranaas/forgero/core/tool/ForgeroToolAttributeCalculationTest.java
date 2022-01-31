package com.sigmundgranaas.forgero.core.tool;

import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.tool.DefaultForgeroToolTest.getDefaultForgeroTool;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeroToolAttributeCalculationTest {
    @Test
    void testDefaultToolDurability() {
        ForgeroTool tool = getDefaultForgeroTool();
        assertTrue(tool.getDurability() > 100);
    }

    @Test
    void testDefaultToolDamage() {
        ForgeroTool tool = getDefaultForgeroTool();
        assertTrue(tool.getAttackDamage() > 1);
    }

    @Test
    void testDefaultToolSpeed() {
        ForgeroTool tool = getDefaultForgeroTool();
        assertTrue(tool.getAttackSpeed() > 1F);
    }
}
