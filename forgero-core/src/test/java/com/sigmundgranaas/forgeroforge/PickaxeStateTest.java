package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgeroforge.testutil.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgeroforge.testutil.Tools.IRON_PICKAXE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.BINDING;

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
    void testUpgradePickaxeIsNewInstance() {
        var pick = IRON_PICKAXE;
        Assertions.assertNotEquals(pick, pick.upgrade(BINDING));
    }

    @Test
    void testUpgradesApply() {
        var pick = IRON_PICKAXE.upgrade(BINDING);
        Assertions.assertEquals(0, pick.stream().applyAttribute(AttributeType.DURABILITY), 100);
    }
}
