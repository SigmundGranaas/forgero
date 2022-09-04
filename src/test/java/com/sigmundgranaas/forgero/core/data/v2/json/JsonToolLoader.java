package com.sigmundgranaas.forgero.core.data.v2.json;

import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.data.Constant.JSON_TEST_PATH;

public class JsonToolLoader {
    static Supplier<JsonResource> JSON_PICKAXE = () -> JsonPOJOLoader.loadPOJO(JSON_TEST_PATH + "pickaxe.json", JsonResource.class).orElseThrow();

    @Test
    void testLoadJsonPickaxe() {
        JsonResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.type, "PICKAXE");
        Assertions.assertNotNull(pickaxe.construct);
        Assertions.assertNotNull(pickaxe.construct.recipe);
        Assertions.assertNotNull(pickaxe.construct.slots);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void multipleSlots() {
        JsonResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.construct.slots.size(), 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void recipeEntries() {
        JsonResource pickaxe = JSON_PICKAXE.get();
        Assertions.assertEquals(pickaxe.construct.recipe.ingredients.size(), 2);
    }
}
