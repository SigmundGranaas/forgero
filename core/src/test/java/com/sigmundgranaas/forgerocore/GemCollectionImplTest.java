package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GemCollectionImplTest {

    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    @Test
    void getGems() {

        var collection = ForgeroRegistry.GEM.list();
        Assertions.assertTrue(collection.size() > 1);
    }

    @Test
    void gemsAreTheSameInstance() {
        var collection = ForgeroRegistry.GEM.list();
        var collection1 = ForgeroRegistry.GEM.list();
        for (int i = 0; i < collection.size(); i++) {
            Assertions.assertEquals(collection.get(i), collection1.get(i));
        }

    }
}