package com.sigmundgranaas.forgero.core.data.v2.json;

import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.sigmundgranaas.forgero.core.data.Constant.JSON_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JsonModelTest {
    static Function<String, JsonModel> JSON_MODEL = (String json) -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + json, JsonModel.class).orElseThrow();

    @Test
    void testLoadJsonConstruct() {
        JsonModel model = JSON_MODEL.apply("model.json");
        assertNotNull(model);
    }

    @Test
    void testModelValues() {
        JsonModel model = JSON_MODEL.apply("model.json");
        assertNotNull(model.modelType);
        assertNotNull(model.models);
        assertTrue(model.models.size() > 0);
        model.models.forEach(Assertions::assertNotNull);
    }
}
