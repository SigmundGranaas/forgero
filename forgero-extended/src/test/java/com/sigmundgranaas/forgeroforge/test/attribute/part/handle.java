package com.sigmundgranaas.forgeroforge.test.attribute.part;

import com.sigmundgranaas.forgeroforge.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.property.AttributeType.DURABILITY;
import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

public class handle extends ForgeroPackageTest {
    @Test
    void testOakHandle() {
        var state = state("forgero:oak-handle");
        var durability = state.stream().applyAttribute(DURABILITY);
        Assertions.assertEquals(10, durability, 10);
    }

    @Test
    void testIronHandle() {
        var state = state("forgero:iron-handle");
        var durability = state.stream().applyAttribute(DURABILITY);
        Assertions.assertEquals(150, durability, 50);
    }

    @Test
    void testStoneHandle() {
        var state = state("forgero:stone-handle");
        var durability = state.stream().applyAttribute(DURABILITY);
        Assertions.assertEquals(50, durability, 50);
    }

    @Test
    void testNetheriteHandle() {
        var state = state("forgero:netherite-handle");
        var durability = state.stream().applyAttribute(DURABILITY);
        Assertions.assertEquals(1000, durability, 200);
    }
}
