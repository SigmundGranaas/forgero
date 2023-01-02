package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonConstructTest {
    static Supplier<ConstructData> JSON_CONSTRUCT = () -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + "construct.json", ConstructData.class).orElseThrow();

    @Test
    void testLoadJsonConstruct() {
        ConstructData construct = JSON_CONSTRUCT.get();
        assertNotNull(construct.recipes().get());
        assertNotNull(construct.slots());
        assertNotNull(construct.target());
    }

    @Test
    void multipleSlots() {
        ConstructData construct = JSON_CONSTRUCT.get();
        assert construct.slots() != null;
        assertTrue(construct.slots().size() > 0);
        construct.slots().forEach(Assertions::assertNotNull);
    }
}
