package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.data.v2.JsonResourceLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;

class ConstructLoaderTest {

    @Test
    void loadConstructs() {
        JsonResourceLoader loader = new JsonResourceLoader(CORE_PATH);
        var pool = loader.load();
        var constructs = pool.stream()
                .filter(resource -> resource.construct().isPresent())
                .toList();

        Assertions.assertTrue(constructs.size() > 0);
    }
}
