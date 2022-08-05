package com.sigmundgranaas.forgero.core.data.v2.json;

import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.sigmundgranaas.forgero.core.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class JsonSlotTest {
    public static Function<String, JsonSlot> JSON_SLOT = (String slot) -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + slot, JsonSlot.class).orElseThrow();

    @Test
    void testLoadJsonSlot() {
        JsonSlot slot = JSON_SLOT.apply("slot_test.json");
        assertNotNull(slot.category);
        assertNotNull(slot.type);
        assertNotNull(slot.upgradeType);
        assertEquals(slot.tier, 1);
    }

    @Test
    void testLoadEmptySlot() {
        JsonSlot slot = JSON_SLOT.apply("slot_test_invalid.json");
        var newSlot = new JsonSlot();
        assertEquals(slot.category, newSlot.category);
        assertEquals(slot.type, newSlot.type);
        assertEquals(slot.upgradeType, newSlot.upgradeType);
        assertEquals(slot.tier, newSlot.tier);
    }
}