package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.JSON_TEST_PATH;

public class JsonToolLoader {
    static Supplier<DataResource> JSON_PICKAXE = () -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + "pickaxe.json", DataResource.class).orElseThrow();

    @Test
    void testLoadJsonPickaxe() {
        DataResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.type(), "PICKAXE");
        Assertions.assertNotNull(pickaxe.construct());
        Assertions.assertNotNull(pickaxe.construct().get().recipes().get());
        Assertions.assertNotNull(pickaxe.construct().get().slots());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void multipleSlots() {
        DataResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.construct().get().slots().size(), 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void recipeEntries() {
        DataResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.construct().get().recipes().get().get(0).ingredients().size(), 2);
    }
}
