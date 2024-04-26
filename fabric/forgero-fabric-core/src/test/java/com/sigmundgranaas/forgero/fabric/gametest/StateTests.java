package com.sigmundgranaas.forgero.fabric.gametest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.NameCompositor;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class StateTests {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeAndAssembleArrowName(TestContext context) {
		var head = StateService.INSTANCE.find("forgero:oak-mastercrafted_arrow_head").get();
		Composite handle = (Composite) StateService.INSTANCE.find("forgero:oak-mastercrafted_handle").get();
		handle = handle.upgrade(StateService.INSTANCE.find("minecraft:pink_dye").get());
		var feather = StateService.INSTANCE.find("minecraft:feather").get();

		var arrow = new Construct.ConstructBuilder().addIngredients(List.of(head, handle, feather)).build();
		assertEquals("forgero:oak-arrow", arrow.identifier());

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeAndAssemblePickaxeName(TestContext context) {
		var head = StateService.INSTANCE.find("forgero:oak-mastercrafted_pickaxe_head").get();
		Composite handle = (Composite) StateService.INSTANCE.find("forgero:oak-mastercrafted_handle").get();
		handle = handle.upgrade(StateService.INSTANCE.find("minecraft:pink_dye").get());

		var pickaxe = new Construct.ConstructBuilder().addIngredients(List.of(head, handle)).build();
		assertEquals("forgero:oak-pickaxe", pickaxe.identifier());

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
	public void testUpgradeAndAssembleSwordName(TestContext context) {
		var head = StateService.INSTANCE.find("forgero:oak-mastercrafted_sword_blade").get();
		Composite handle = (Composite) StateService.INSTANCE.find("forgero:oak-mastercrafted_handle").get();
		handle = handle.upgrade(StateService.INSTANCE.find("minecraft:pink_dye").get());

		var sword = new Construct.ConstructBuilder().addIngredients(List.of(head, handle)).build();
		assertEquals("forgero:oak-sword", sword.identifier());

		context.complete();
	}

	private void nameTest(String expectedName, String... ingredients) {
		assertEquals(expectedName, NameCompositor.of().compositeName(Arrays.stream(ingredients).map(StateService.INSTANCE::find).flatMap(Optional::stream).toList()));
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "state_name_tests", required = true)
	public void testCommonToolNames(TestContext context) {
		nameTest("iron-pickaxe", "forgero:iron-pickaxe_head", "forgero:oak-handle");
		nameTest("iron-pickaxe", "forgero:iron-pickaxe_head", "forgero:iron-handle");
		nameTest("iron-pickaxe", "forgero:iron-pickaxe_head", "forgero:iron-mastercrafted_handle");
		nameTest("iron-pickaxe", "forgero:iron-mastercrafted_pickaxe_head", "forgero:iron-mastercrafted_handle");

		nameTest("iron-sword", "forgero:iron-sword_blade", "forgero:oak-handle");
		nameTest("iron-sword", "forgero:iron-knife_blade", "forgero:oak-handle");
		nameTest("iron-sword", "forgero:iron-sword_blade", "forgero:iron-handle");
		nameTest("iron-sword", "forgero:iron-sword_blade", "forgero:iron-mastercrafted_handle");
		nameTest("iron-sword", "forgero:iron-mastercrafted_sword_blade", "forgero:iron-mastercrafted_handle");

		nameTest("iron-pickaxe_head", "forgero:pickaxe_head-schematic", "minecraft:iron");

		nameTest("iron-axe", "forgero:iron-axe_head", "forgero:oak-handle");
		nameTest("iron-hoe", "forgero:iron-hoe_head", "forgero:oak-handle");
		nameTest("iron-shovel", "forgero:iron-shovel_head", "forgero:oak-handle");
		nameTest("oak-bow", "forgero:oak-bow_limb", "minecraft:string");

		context.complete();
	}
}
