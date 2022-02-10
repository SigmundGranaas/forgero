package com.sigmundgranaas.forgero.core.gem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GemCollectionImplTest {

    @Test
    void getGems() {
        var collection = GemCollection.INSTANCE.getGems();
        Assertions.assertTrue(collection.size() > 1);
    }

    @Test
    void gemsAreTheSameInstance() {
        var collection = GemCollection.INSTANCE.getGems();
        var collection1 = GemCollection.INSTANCE.getGems();
        for (int i = 0; i < collection.size(); i++) {
            Assertions.assertEquals(collection.get(i), collection1.get(i));
        }

    }
}