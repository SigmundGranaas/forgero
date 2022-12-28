package com.sigmundgranaas.forgeroforge.test.attribute.tool;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgeroforge.test.util.AttributeTester;
import com.sigmundgranaas.forgeroforge.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.property.AttributeType.*;
import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

/**
 * All vanilla tools should stay within some distance to their minecraft counterparts
 */
public class ExtendedPickaxetest extends ForgeroPackageTest {
    @Test
    void testOakPickaxe() {
        AttributeTester.tester(state("forgero:oak-pickaxe"))
                .add(DURABILITY, 50, 10)
                .add(MINING_SPEED, 2, 1)
                .add(RARITY, 12, 5)
                .add(ATTACK_DAMAGE, 3, 0.5)
                .add(ATTACK_SPEED, -2.8, 0.5)
                .run();
    }

    @Test
    void testIronPickaxe() {
        AttributeTester.tester(state("forgero:iron-pickaxe"))
                .add(DURABILITY, 250, 50)
                .add(MINING_SPEED, 6, 1)
                .add(RARITY, 40, 10)
                .add(ATTACK_DAMAGE, 4, 0.5)
                .add(ATTACK_SPEED, -2.8, 0.5)
                .run();
    }

    @Test
    void testGoldPickaxe() {
        AttributeTester.tester(state("forgero:gold-pickaxe"))
                .add(DURABILITY, 41, 10)
                .add(MINING_SPEED, 12, 1)
                .add(RARITY, 60, 10)
                .add(ATTACK_DAMAGE, 3, 0.5)
                .add(ATTACK_SPEED, -2.8, 0.5)
                .run();
    }

    @Test
    void testDiamondPickaxe() {
        AttributeTester.tester(state("forgero:diamond-pickaxe"))
                .add(DURABILITY, 1500, 300)
                .add(MINING_SPEED, 8, 1)
                .add(RARITY, 80, 10)
                .add(ATTACK_DAMAGE, 5, 0.5)
                .add(ATTACK_SPEED, -2.8, 0.5)
                .run();
    }

    @Test
    void testNetheritePickaxe() {
        AttributeTester.tester(state("forgero:netherite-pickaxe"))
                .add(DURABILITY, 2100, 300)
                .add(MINING_SPEED, 9, 1)
                .add(RARITY, 110, 10)
                .add(ATTACK_DAMAGE, 6, 1.5)
                .add(ATTACK_SPEED, -2.8, 0.5)
                .run();
    }

    @Test
    void testDiamondPickaxeIronHandle() {
        var pickaxe = Composite.builder()
                .id("forgero:diamond-pickaxe")
                .addIngredient(state("forgero:iron-handle"))
                .addIngredient(state("forgero:diamond-pickaxe_head"))
                .build();

        AttributeTester.tester(pickaxe)
                .add(DURABILITY, 1600, 200)
                .add(MINING_SPEED, 9, 1)
                .add(RARITY, 88, 10)
                .add(ATTACK_DAMAGE, 6, 1.5)
                .add(ATTACK_SPEED, -3.0, 0.3)
                .run();
    }
}
