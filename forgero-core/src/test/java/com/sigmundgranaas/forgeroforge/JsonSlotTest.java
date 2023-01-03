package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.SlotData;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;


class JsonSlotTest {
    public static Function<String, SlotData> JSON_SLOT = (String slot) -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + slot, SlotData.class).orElseThrow();

    @Test
    void testLoadJsonSlot() {
        SlotData slot = JSON_SLOT.apply("slot_test.json");
        Assertions.assertNotNull(slot.category());
        Assertions.assertNotNull(slot.type());
        Assertions.assertNotNull(slot.upgradeType());
        assertEquals(0, slot.tier());
    }

    @Test
    void testLoadEmptySlot() {
        SlotData slot = JSON_SLOT.apply("slot_test_invalid.json");
        var newSlot = SlotData.builder().build();
        assertEquals(slot.category(), newSlot.category());
        assertEquals(slot.type(), newSlot.type());
        assertEquals(slot.upgradeType(), newSlot.upgradeType());
    }
}