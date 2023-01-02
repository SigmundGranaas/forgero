package com.sigmundgranaas.forgeroforge.test.attribute.tool;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgeroforge.test.util.AttributeTester;
import com.sigmundgranaas.forgeroforge.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.property.AttributeType.*;
import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

/**
 * All vanilla tools should stay within some distance to their minecraft counterparts
 */
public class ExtendedAxeTest extends ForgeroPackageTest {
    @Test
    void testOakAxe() {
        AttributeTester.tester(state("forgero:oak-axe"))
                .add(DURABILITY, 50, 10)
                .add(MINING_SPEED, 2, 1)
                .add(RARITY, 12, 5)
                .add(ATTACK_DAMAGE, 5, 0.5)
                .add(ATTACK_SPEED, -3.0, 0.5)
                .run();
    }

    @Test
    void testIronAxe() {
        AttributeTester.tester(state("forgero:iron-axe"))
                .add(DURABILITY, 250, 50)
                .add(MINING_SPEED, 6, 1)
                .add(RARITY, 40, 10)
                .add(ATTACK_DAMAGE, 8, 0.5)
                .add(ATTACK_SPEED, -3.0, 0.5)
                .run();
    }

    @Test
    void testGoldAxe() {
        AttributeTester.tester(state("forgero:gold-axe"))
                .add(DURABILITY, 41, 10)
                .add(MINING_SPEED, 12, 1)
                .add(RARITY, 60, 10)
                .add(ATTACK_DAMAGE, 5, 0.5)
                .add(ATTACK_SPEED, -3.0, 0.5)
                .run();
    }

    @Test
    void testDiamondAxe() {
        AttributeTester.tester(state("forgero:diamond-axe"))
                .add(DURABILITY, 1500, 300)
                .add(MINING_SPEED, 8, 1)
                .add(RARITY, 80, 10)
                .add(ATTACK_DAMAGE, 8, 0.5)
                .add(ATTACK_SPEED, -3.0, 0.5)
                .run();
    }

    @Test
    void testNetheriteAxe() {
        AttributeTester.tester(state("forgero:netherite-axe"))
                .add(DURABILITY, 2100, 300)
                .add(MINING_SPEED, 9, 1)
                .add(RARITY, 110, 10)
                .add(ATTACK_DAMAGE, 10, 1.5)
                .add(ATTACK_SPEED, -3.2, 0.5)
                .run();
    }

    @Test
    void testDiamondAxeIronHandle() {
        var pickaxe = Composite.builder()
                .id("forgero:diamond-axe")
                .addIngredient(state("forgero:iron-handle"))
                .addIngredient(state("forgero:diamond-axe_head"))
                .build();

        AttributeTester.tester(pickaxe)
                .add(DURABILITY, 1600, 200)
                .add(MINING_SPEED, 9, 1)
                .add(RARITY, 88, 10)
                .add(ATTACK_DAMAGE, 8, 1.5)
                .add(ATTACK_SPEED, -3.4, 0.3)
                .run();
    }
}
