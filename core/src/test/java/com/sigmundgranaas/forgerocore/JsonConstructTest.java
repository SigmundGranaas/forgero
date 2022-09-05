package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.data.v2.json.JsonConstruct;
import com.sigmundgranaas.forgerocore.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgerocore.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonConstructTest {
    static Supplier<JsonConstruct> JSON_CONSTRUCT = () -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + "construct.json", JsonConstruct.class).orElseThrow();

    @Test
    void testLoadJsonConstruct() {
        JsonConstruct construct = JSON_CONSTRUCT.get();
        assertNotNull(construct.recipe);
        assertNotNull(construct.slots);
        assertNotNull(construct.target);
    }

    @Test
    void multipleSlots() {
        JsonConstruct construct = JSON_CONSTRUCT.get();
        assert construct.slots != null;
        assertTrue(construct.slots.size() > 0);
        construct.slots.forEach(Assertions::assertNotNull);
    }
}
