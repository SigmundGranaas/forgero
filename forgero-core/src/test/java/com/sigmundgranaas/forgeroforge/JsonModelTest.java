package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.sigmundgranaas.forgero.resource.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JsonModelTest {
    static Function<String, ModelData> JSON_MODEL = (String json) -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + json, ModelData.class).orElseThrow();

    @Test
    void testLoadJsonConstruct() {
        ModelData model = JSON_MODEL.apply("model.json");
        assertNotNull(model);
    }

    @Test
    void testModelValues() {
        ModelData model = JSON_MODEL.apply("model.json");
        assertNotNull(model.getModelType());
        assertNotNull(model.getVariants());
        assertTrue(model.getVariants().size() > 0);
        model.getVariants().forEach(Assertions::assertNotNull);
    }
}
