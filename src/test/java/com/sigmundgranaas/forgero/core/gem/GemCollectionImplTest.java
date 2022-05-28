package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.LegacyForgeroRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GemCollectionImplTest {

    @Test
    void getGems() {
        var collection = LegacyForgeroRegistry.getInstance().gemCollection().getGems();
        Assertions.assertTrue(collection.size() > 1);
    }

    @Test
    void gemsAreTheSameInstance() {
        var collection = LegacyForgeroRegistry.getInstance().gemCollection().getGems();
        var collection1 = LegacyForgeroRegistry.getInstance().gemCollection().getGems();
        for (int i = 0; i < collection.size(); i++) {
            Assertions.assertEquals(collection.get(i), collection1.get(i));
        }

    }
}