package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.name.NameResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deep tests for the name resolution system.
 * <p>
 * These tests verify that players see properly translated names in-game,
 * not raw component IDs or translation keys.
 * <p>
 * Each test asserts EXACT expected values - if the name doesn't match
 * exactly what players should see, the test fails.
 */
public class NameResolutionGameTest implements ForgeroGameTest {

	// ============================================================
	// Core NameResolver Unit Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_resolver_produces_exact_translation_for_simple_tool(TestContext context) {
		String resolved = NameResolver.resolve("iron-pickaxe").getString();

		assertEquals("Iron Pickaxe", resolved,
				"NameResolver must produce 'Iron Pickaxe' from 'iron-pickaxe'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_resolver_produces_exact_translation_for_single_element(TestContext context) {
		String resolved = NameResolver.resolve("iron").getString();

		assertEquals("Iron", resolved,
				"NameResolver must produce 'Iron' from 'iron'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_resolver_produces_exact_translation_for_part(TestContext context) {
		String resolved = NameResolver.resolve("iron-pickaxe_head").getString();

		assertEquals("Iron Pickaxe Head", resolved,
				"NameResolver must produce 'Iron Pickaxe Head' from 'iron-pickaxe_head'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_resolver_produces_exact_translation_for_extended_weapon(TestContext context) {
		String resolved = NameResolver.resolve("iron-spear").getString();

		assertEquals("Iron Spear", resolved,
				"NameResolver must produce 'Iron Spear' from 'iron-spear'");

		context.complete();
	}

	// ============================================================
	// ItemStack Display Name Tests - Base Tools
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_displays_as_Iron_Pickaxe(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-pickaxe");

		assertEquals("Iron Pickaxe", stack.getName().getString(),
				"Iron pickaxe must display as 'Iron Pickaxe' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_sword_displays_as_Diamond_Sword(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:diamond-sword");

		assertEquals("Diamond Sword", stack.getName().getString(),
				"Diamond sword must display as 'Diamond Sword' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_axe_displays_as_Netherite_Axe(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:netherite-axe");

		assertEquals("Netherite Axe", stack.getName().getString(),
				"Netherite axe must display as 'Netherite Axe' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gold_shovel_displays_as_Gold_Shovel(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:gold-shovel");

		assertEquals("Gold Shovel", stack.getName().getString(),
				"Gold shovel must display as 'Gold Shovel' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void oak_hoe_displays_as_Oak_Hoe(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:oak-hoe");

		assertEquals("Oak Hoe", stack.getName().getString(),
				"Oak hoe must display as 'Oak Hoe' in inventory");

		context.complete();
	}

	// ============================================================
	// ItemStack Display Name Tests - Extended Weapons
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_spear_displays_as_Iron_Spear(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-spear");

		assertEquals("Iron Spear", stack.getName().getString(),
				"Iron spear must display as 'Iron Spear' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_mace_displays_as_Diamond_Mace(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:diamond-mace");

		assertEquals("Diamond Mace", stack.getName().getString(),
				"Diamond mace must display as 'Diamond Mace' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_spear_displays_as_Netherite_Spear(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:netherite-spear");

		assertEquals("Netherite Spear", stack.getName().getString(),
				"Netherite spear must display as 'Netherite Spear' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_mace_displays_as_Iron_Mace(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-mace");

		assertEquals("Iron Mace", stack.getName().getString(),
				"Iron mace must display as 'Iron Mace' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gold_spear_displays_as_Gold_Spear(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:gold-spear");

		assertEquals("Gold Spear", stack.getName().getString(),
				"Gold spear must display as 'Gold Spear' in inventory");

		context.complete();
	}

	// ============================================================
	// ItemStack Display Name Tests - Parts
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_head_displays_as_Iron_Pickaxe_Head(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-pickaxe_head");

		assertEquals("Iron Pickaxe Head", stack.getName().getString(),
				"Iron pickaxe head must display as 'Iron Pickaxe Head' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void oak_handle_displays_as_Oak_Handle(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:oak-handle");

		assertEquals("Oak Handle", stack.getName().getString(),
				"Oak handle must display as 'Oak Handle' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_sword_blade_displays_as_Diamond_Sword_Blade(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:diamond-sword_blade");

		assertEquals("Diamond Sword Blade", stack.getName().getString(),
				"Diamond sword blade must display as 'Diamond Sword Blade' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_spear_head_displays_as_Iron_Spear_Head(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-spear_head");

		assertEquals("Iron Spear Head", stack.getName().getString(),
				"Iron spear head must display as 'Iron Spear Head' in inventory");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void oak_binding_displays_as_Oak_Binding(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:oak-binding");

		assertEquals("Oak Binding", stack.getName().getString(),
				"Oak binding must display as 'Oak Binding' in inventory");

		context.complete();
	}

	// ============================================================
	// ItemStack Display Name Tests - Mining Tools (Head Parts)
	// Mining tools use part variants that work with base equipment.
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_hammer_head_displays_as_Iron_Hammer_Head(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-hammer_head");

		assertEquals("Iron Hammer Head", stack.getName().getString(),
				"Iron hammer head must display as 'Iron Hammer Head' in inventory");

		context.complete();
	}

	// ============================================================
	// Material Coverage Tests - All materials produce correct names
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_vanilla_materials_produce_correct_pickaxe_names(TestContext context) {
		assertToolName(context, "forgero:oak-pickaxe", "Oak Pickaxe");
		assertToolName(context, "forgero:stone-pickaxe", "Stone Pickaxe");
		assertToolName(context, "forgero:iron-pickaxe", "Iron Pickaxe");
		assertToolName(context, "forgero:gold-pickaxe", "Gold Pickaxe");
		assertToolName(context, "forgero:diamond-pickaxe", "Diamond Pickaxe");
		assertToolName(context, "forgero:netherite-pickaxe", "Netherite Pickaxe");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_extended_weapon_types_produce_correct_names(TestContext context) {
		assertToolName(context, "forgero:iron-spear", "Iron Spear");
		assertToolName(context, "forgero:iron-mace", "Iron Mace");

		context.complete();
	}

	// ============================================================
	// Negative Tests - Ensure broken names are caught
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_must_not_contain_namespace_prefix(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-pickaxe");
		String name = stack.getName().getString();

		assertFalse(name.contains("forgero:"),
				"Display name must never contain 'forgero:' namespace prefix, got: " + name);
		assertFalse(name.contains(":"),
				"Display name must never contain ':' character, got: " + name);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_must_not_be_raw_component_id(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-spear");
		String name = stack.getName().getString();

		assertNotEquals("iron-spear", name,
				"Display name must not be raw component ID");
		assertFalse(name.contains("-"),
				"Display name must not contain hyphens (indicates untranslated ID), got: " + name);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void name_must_not_be_translation_key(TestContext context) {
		ItemStack stack = getItemStackOrFail(context, "forgero:iron-pickaxe");
		String name = stack.getName().getString();

		assertFalse(name.startsWith("item."),
				"Display name must not be a translation key, got: " + name);
		assertFalse(name.contains(".forgero."),
				"Display name must not contain translation key fragment, got: " + name);

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	/**
	 * Gets an ItemStack from a component ID, failing the test if not found.
	 * This is a fail-fast helper - if the component or item doesn't exist,
	 * the test fails immediately with a clear error message.
	 */
	private ItemStack getItemStackOrFail(TestContext context, String componentId) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component component = ctx.component(componentId)
				.orElseThrow(() -> new AssertionError(
						"Component '" + componentId + "' must exist in registry"));

		return ctx.toStack(component)
				.orElseThrow(() -> new AssertionError(
						"Component '" + componentId + "' must convert to ItemStack"));
	}

	/**
	 * Asserts that a tool displays the expected name.
	 * Helper for batch testing multiple tools.
	 */
	private void assertToolName(TestContext context, String componentId, String expectedName) {
		ItemStack stack = getItemStackOrFail(context, componentId);
		String actualName = stack.getName().getString();

		assertEquals(expectedName, actualName,
				"Tool " + componentId + " must display as '" + expectedName + "'");
	}
}
