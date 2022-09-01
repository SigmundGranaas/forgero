package com.sigmundgranaas.forgero.core.data.v2.json;

import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.sigmundgranaas.forgero.core.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class JsonSchematicTest {
    public static Function<String, JsonResource> SCHEMATIC_JSON = (String slot) -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + slot, JsonResource.class).orElseThrow();


    @Test
    void testLoadSchematicResource() {
        assertNotNull(SCHEMATIC_JSON.apply("schematic_example.json"));
    }

    @Test
    void testJsonConstructFromSchematicData() {
        var schematicData = SCHEMATIC_JSON.apply("schematic_example.json");
        assertNotNull(schematicData.construct);
        assertEquals(schematicData.construct.target, Constants.THIS);
    }

    @Test
    void createValidStaticDataFromJson() {
        var schematicData = SCHEMATIC_JSON.apply("schematic_example.json");
        assertNotNull(schematicData.construct);

    }
}
