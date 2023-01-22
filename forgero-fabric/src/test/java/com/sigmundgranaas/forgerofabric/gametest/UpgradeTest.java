package com.sigmundgranaas.forgerofabric.gametest;

import com.sigmundgranaas.forgerofabric.testutil.RecipeTester;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgerofabric.gametest.RecipeTest.assertTrue;

public class UpgradeTest {


    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradeDiamondPickaxeHead(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-pickaxe_head", "minecraft:iron_ingot", context);
        assertTrue(test, "Unable to upgrade diamond pickaxe head with iron");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradePickaxeHeadWithWrongItem(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-pickaxe_head", "minecraft:stick", context);
        assertTrue(() -> !test.get(), "Can upgrade with invalid items");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradePickaxeWithBinding(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-pickaxe", "oak-binding", context);
        assertTrue(test, "Unable to upgrade Diamond pickaxe with binding");
        context.complete();
    }
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradePickaxeWithSpikedBinding(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-pickaxe", "oak-spiked_binding", context);
        assertTrue(test, "Unable to upgrade Diamond pickaxe with spiked binding");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradeSwordGuard(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-sword", "diamond-sword_guard", context);
        assertTrue(test, "Unable to upgrade Diamond sword with sword guard");
        context.complete();
    }
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testUpgradeWithAgileSwordGuard(TestContext context) {
        var test = RecipeTester.smithingUpgrade("diamond-sword", "diamond-agile_sword_guard", context);
        assertTrue(test, "Unable to upgrade Diamond sword with agile sword guard");
        context.complete();
    }
}
