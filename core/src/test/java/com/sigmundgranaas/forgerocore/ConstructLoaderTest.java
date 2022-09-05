package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.data.v2.json.JsonResource;
import com.sigmundgranaas.forgerocore.resourceloader.JsonPathLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

class ConstructLoaderTest {

    @Test
    void loadConstructs() throws URISyntaxException {
        JsonPoolLoader loader = new JsonPoolLoader(JsonPathLoader.getResourcesInFolder());
        var pool = loader.loadPojos();
        var constructs = pool.stream()
                .filter(resource -> resource.construct != null)
                .map(JsonResource::applyDefaults)
                .toList();

        Assertions.assertTrue(constructs.size() > 0);
    }
}
