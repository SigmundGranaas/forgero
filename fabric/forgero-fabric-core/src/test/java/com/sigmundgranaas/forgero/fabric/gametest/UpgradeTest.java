package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.fabric.gametest.RecipeTest.assertFalse;
import static com.sigmundgranaas.forgero.fabric.gametest.RecipeTest.assertTrue;

import java.util.List;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.testutil.RecipeTester;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class UpgradeTest {


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeDiamondPickaxeHead(TestContext context) {
		var test = RecipeTester.smithingUpgrade("diamond-pickaxe_head", "minecraft:iron_ingot", context);
		assertTrue(test, "Unable to upgrade diamond pickaxe head with iron");
		context.complete();
	}
	
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeMastercraftedHandleWithDye(TestContext context) {
		var test = RecipeTester.smithingUpgrade("oak-mastercrafted_handle", "minecraft:pink_dye", context);
		assertTrue(test, "Unable to upgrade mastercrafted handle with dye");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeMastercraftedHandleArrowWithDye(TestContext context) {
		var head = StateService.INSTANCE.find("forgero:oak-mastercrafted_arrow_head").get();
		Composite handle = (Composite) StateService.INSTANCE.find("forgero:oak-mastercrafted_handle").get();
		handle = handle.upgrade(StateService.INSTANCE.find("minecraft:pink_dye").get());
		var feather = StateService.INSTANCE.find("minecraft:feather").get();

		var arrow = new Construct.ConstructBuilder().addIngredients(List.of(head, handle, feather)).id("forgero:oak-arrow").build();

		var test = RecipeTester.smithingUpgrade(arrow, "minecraft:pink_dye", context);
		assertFalse(test, "Should not be able to dye arrow directly");
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

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeWithScrappyBinding(TestContext context) {
		var test = RecipeTester.smithingUpgrade("diamond-pickaxe", "leather-scrappy_binding", context);
		assertTrue(test, "Unable to upgrade pickaxe with scrappy binding");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test")
	public void testUpgradeWithGem(TestContext context) {
		var test = RecipeTester.smithingUpgrade("oak-binding", "redstone-gem", context);
		assertTrue(test, "Unable to oak binding with redstone gem");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradeDiamondPickaxeHead(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-pickaxe_head", "minecraft:iron_ingot", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade diamond pickaxe head with iron");

		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradePickaxeHeadWithWrongItem(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-pickaxe_head", "minecraft:stick", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(() -> !test.get(), "Can upgrade with invalid items");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradePickaxeWithBinding(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-pickaxe", "oak-binding", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade Diamond pickaxe with binding");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradePickaxeWithSpikedBinding(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-pickaxe", "oak-spiked_binding", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade Diamond pickaxe with spiked binding");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradeSwordGuard(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-sword", "diamond-sword_guard", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade Diamond sword with sword guard");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test", required = true)
	public void testCraftingTableUpgradeWithAgileSwordGuard(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-sword", "diamond-agile_sword_guard", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade Diamond sword with agile sword guard");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test")
	public void testCraftingTableUpgradeWithScrappyBinding(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("diamond-pickaxe", "leather-scrappy_binding", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to upgrade pickaxe with scrappy binding");

		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "crafting_table_upgrade_recipe_test")
	public void testCraftingTableUpgradeWithGem(TestContext context) {
		var test = RecipeTester.craftingTableUpgrade("oak-binding", "redstone-gem", context);
		if (ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable) {
			assertTrue(test, "Unable to oak binding with redstone gem");

		}
		context.complete();
	}
}
