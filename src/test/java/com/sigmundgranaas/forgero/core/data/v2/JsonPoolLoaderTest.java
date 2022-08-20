package com.sigmundgranaas.forgero.core.data.v2;

import com.sigmundgranaas.forgero.core.resourceloader.JsonPathLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

class JsonPoolLoaderTest {

    @Test
    void loadPojos() throws URISyntaxException {
        JsonPoolLoader loader = new JsonPoolLoader(JsonPathLoader.getResourcesInFolder());
        Assertions.assertTrue(loader.loadPojos().size() > 0);
    }
}