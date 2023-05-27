package com.sigmundgranaas.forgerofabric.gametest;

import java.util.function.Supplier;

import com.sigmundgranaas.forgerofabric.testutil.RecipeTester;

import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class RecipeTest {
	public static void assertTrue(Supplier<Boolean> test, String errorMessage) {
		if (!test.get()) {
			throw new GameTestException(errorMessage);
		}
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCraftIronPickaxe(TestContext context) {
		var test = RecipeTester.ofTool("iron-pickaxe_head", "oak-handle", "forgero:iron-pickaxe", context);
		assertTrue(test, "unable to craft iron pickaxe");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCraftOakSword(TestContext context) {
		var test = RecipeTester.ofTool("oak-sword_blade", "oak-handle", "forgero:oak-sword", context);
		assertTrue(test, "unable to craft oak sword");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCraftOakKatana(TestContext context) {
		var test = RecipeTester.ofTool("oak-katana_blade", "oak-handle", "oak-sword", context);
		assertTrue(test, "unable to craft oak katana");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCraftPickaxeWithBinding(TestContext context) {
		var test = RecipeTester.ofTool("oak-pickaxe_head", "oak-binding", "oak-handle", "oak-pickaxe", context);
		assertTrue(test, "unable to craft oak pickaxe with binding");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCreatePickaxeFromSchematic(TestContext context) {
		var test = RecipeTester.ofPart("pickaxe_head-schematic", "minecraft:oak_planks", 3, "oak-pickaxe_head", context);
		assertTrue(test, "Unable to craft oak pickaxe head");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCrateMasterCraftedSwordBlade(TestContext context) {
		var test = RecipeTester.ofPart("mastercrafted_sword_blade-schematic", "minecraft:oak_planks", 3, "oak-mastercrafted_sword_blade", context);
		assertTrue(test, "Unable to craft oak sword blade");
		context.complete();
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testCraftHandle(TestContext context) {
		var test = RecipeTester.ofPart("handle-schematic", "minecraft:diamond", 2, "diamond-handle", context);
		assertTrue(test, "Unable to craft diamond handle");
		context.complete();
	}
}
