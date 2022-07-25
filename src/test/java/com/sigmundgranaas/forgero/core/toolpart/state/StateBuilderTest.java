package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.exception.MissingForgeroResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.Util.INIT_TEST_REGISTRY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateBuilderTest {
    @BeforeEach
    void initialiseResources() {
        INIT_TEST_REGISTRY.run();
    }

    @Test
    void createStateFromStrings() throws MissingForgeroResourceException {
        var state = ofErrorAble("oak", "pickaxe").build();
        assertEquals(state.getPrimarySlot().getResourceName(), "oak");
    }

    @Test
    void addSecondaryMaterialUpgradeToBuilder() throws MissingForgeroResourceException {
        var state = ofErrorAble("oak", "pickaxe").add("iron").build();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "iron");
    }

    @Test
    void addGemToBuilder() throws MissingForgeroResourceException {
        var state = ofErrorAble("oak", "pickaxe").add("lapiz-gem").build();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "lapiz-gem");
    }

    @Test
    void replaceGemOnBuilder() throws MissingForgeroResourceException {
        var state = ofErrorAble("oak", "pickaxe")
                .add("lapiz-gem")
                .set(0, "redstone-gem")
                .build();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "redstone-gem");

    }
}
