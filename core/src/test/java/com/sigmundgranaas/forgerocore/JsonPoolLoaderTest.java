package com.sigmundgranaas.forgerocore;


import com.sigmundgranaas.forgerocore.resourceloader.JsonPathLoader;
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