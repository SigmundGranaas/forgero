package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GemCollectionImplTest {

    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new ForgeroResourceInitializer());
    }

    @Test
    void getGems() {

        var collection = ForgeroRegistry.GEM.list();
        Assertions.assertTrue(collection.size() > 1);
    }

    @Test
    void gemsAreTheSameInstance() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new ForgeroResourceInitializer());
        var collection = ForgeroRegistry.GEM.list();
        var collection1 = ForgeroRegistry.GEM.list();
        for (int i = 0; i < collection.size(); i++) {
            Assertions.assertEquals(collection.get(i), collection1.get(i));
        }

    }
}