package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.testutil.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.testutil.ToolParts.HANDLE;

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
    void testHandleTypeSchematicFail() {
        var handle = HANDLE;
        Assertions.assertFalse(handle.test(Types.SCHEMATIC));
    }
}
