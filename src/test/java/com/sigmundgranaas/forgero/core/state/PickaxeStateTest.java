package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.testutil.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.testutil.Tools.IRON_PICKAXE;

public class PickaxeStateTest {
    @Test
    void testCreatePickaxeState() {
        var pick = IRON_PICKAXE;
        Assertions.assertEquals("iron-pickaxe", pick.name());
    }

    @Test
    void textPickaxeProperties() {
        var pick = IRON_PICKAXE;
        Assertions.assertEquals(4, pick.getProperties().size());
    }

    @Test
    void testPickaxeType() {
        var pick = IRON_PICKAXE;
        Assertions.assertTrue(pick.test(Types.TOOL));
    }

    @Test
    void testPickaxeTypeWoodTrue() {
        var pick = IRON_PICKAXE;
        Assertions.assertTrue(pick.test(Types.WOOD));
    }

    @Test
    void testPickaxeTypeFailForRandom() {
        var pick = IRON_PICKAXE;
        Assertions.assertFalse(pick.test(Types.RANDOM));
    }

    @Test
    void testPickaxeTypeFailsForSchematic() {
        var pick = IRON_PICKAXE;
        Assertions.assertFalse(pick.test(Types.SCHEMATIC));
    }
}
