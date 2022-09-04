package com.sigmundgranaas.forgero.core.data.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;
import com.sigmundgranaas.forgero.core.resourceloader.JsonPathLoader;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

class SchematicLoaderTest {

    @Test
    void loadSchematics() throws URISyntaxException {
        JsonPoolLoader loader = new JsonPoolLoader(JsonPathLoader.getResourcesInFolder());
        ResourcePool pool = new ResourcePool(ImmutableList.<JsonResource>builder().addAll(loader.loadPojos()).build());
        var tree = pool.createLoadedTypeTree();

    }
}
