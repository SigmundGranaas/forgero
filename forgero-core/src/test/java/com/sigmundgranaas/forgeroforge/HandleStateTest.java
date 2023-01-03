package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgeroforge.testutil.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.REDSTONE_GEM;

public class HandleStateTest {

    @Test
    void testCreateHandleState() {
        var handle = HANDLE;
        Assertions.assertEquals("oak-handle", handle.name());
    }

    @Test
    void testHandleProperties() {
        var handle = HANDLE;
        Assertions.assertTrue(handle.getProperties().size() > 0);
    }

    @Test
    void testHandleType() {
        var handle = HANDLE;
        Assertions.assertTrue(handle.test(Types.HANDLE));
    }

    @Test
    void testHandleTypeMaterial() {
        var handle = HANDLE;
        Assertions.assertTrue(handle.test(Types.MATERIAL));
    }

    @Test
    void testHandleUpgradeIsNotMutated() {
        var handle = HANDLE;
        Assertions.assertNotEquals(handle, handle.upgrade(REDSTONE_GEM));
    }

    @Test
    void testHandleUpgradeAppliesProperty() {
        var handle = HANDLE.upgrade(REDSTONE_GEM);
        Assertions.assertEquals(11, handle.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
    }
}
