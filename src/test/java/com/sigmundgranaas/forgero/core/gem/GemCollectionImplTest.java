package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GemCollectionImplTest {

    @Test
    void getGems() {
        var collection = ForgeroRegistry.getInstance().gemCollection().getGems();
        Assertions.assertTrue(collection.size() > 1);
    }

    @Test
    void gemsAreTheSameInstance() {
        var collection = ForgeroRegistry.getInstance().gemCollection().getGems();
        var collection1 = ForgeroRegistry.getInstance().gemCollection().getGems();
        for (int i = 0; i < collection.size(); i++) {
            Assertions.assertEquals(collection.get(i), collection1.get(i));
        }

    }
}