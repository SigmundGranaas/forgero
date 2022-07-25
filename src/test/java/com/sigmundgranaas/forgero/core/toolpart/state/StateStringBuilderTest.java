package com.sigmundgranaas.forgero.core.toolpart.state;

import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.toolpart.state.StateStringBuilder.HEAD;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateStringBuilderTest extends ForgeroTest {

    @Test
    void createStateFromStrings(){
        var state = HEAD("oak", "pickaxe").build().orElseThrow();
        assertEquals(state.getPrimarySlot().getResourceName(), "oak");
    }

    @Test
    void addSecondaryMaterialUpgradeToBuilder() {
        var state = HEAD("oak", "pickaxe").add("iron").build().orElseThrow();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "iron");
    }

    @Test
    void addGemToBuilder() {
        var state = HEAD("oak", "pickaxe").add("lapiz-gem").build().orElseThrow();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "lapiz-gem");
    }

    @Test
    void replaceGemOnBuilder() {
        var state = HEAD("oak", "pickaxe")
                .add("lapiz-gem")
                .set(0, "redstone-gem")
                .build()
                .orElseThrow();
        assertEquals(state.getFilledSecondaryMaterialSlots().iterator().next().get().getResourceName(), "redstone-gem");
    }
}
