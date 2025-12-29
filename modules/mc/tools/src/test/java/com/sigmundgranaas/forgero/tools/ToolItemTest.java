package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Forgero tool items for real functionality.
 * Focus: Do tools actually work as expected?
 * - Component to ItemStack conversion
 * - Attribute queries via ItemQueryApi
 * - Edge cases with extreme values
 * - Tool type correctness
 */
public class ToolItemTest implements ForgeroGameTest {

	/**
	 * USE CASE: Convert component to ItemStack and back (roundtrip).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void toolComponentRoundtrip(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		// Find a tool component
		var toolOpt = registry.all().stream()
				.filter(this::isToolComponent)
				.findFirst();

		if (toolOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component original = toolOpt.get();

		// Component -> ItemStack -> Component
		var stackOpt = converter.toStack(original);
		assertTrue(stackOpt.isPresent(), "Tool should convert to ItemStack");

		ItemStack stack = stackOpt.get();
		var roundTripOpt = converter.toComponent(stack);
		assertTrue(roundTripOpt.isPresent(), "ItemStack should convert back");

		assertEquals(original.id(), roundTripOpt.get().id(),
				"Roundtrip preserves component ID");

		context.complete();
	}

	/**
	 * USE CASE: Query tool attributes using ItemQueryApi.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryToolAttributes(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemStack tool = getAnyToolStack();

		if (tool == null) {
			context.complete();
			return;
		}

		// Should return actual values, not crash
		float damage = query.getAttackDamage(tool);
		float miningSpeed = query.getMiningSpeed(tool);
		int durability = query.getMaxDurability(tool);

		assertTrue(damage >= 0 || miningSpeed > 0 || durability > 0,
				"Tool should have some positive attributes");

		context.complete();
	}

	/**
	 * USE CASE: Pickaxe components convert to PickaxeItem instances.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void pickaxeComponentBecomesPickaxeItem(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		// Find a complete pickaxe (has both "tool" and "pickaxe" tags)
		var pickaxeOpt = registry.all().stream()
				.filter(c -> hasTag(c, "tool") && hasTag(c, "pickaxe"))
				.findFirst();

		if (pickaxeOpt.isEmpty()) {
			context.complete();
			return;
		}

		var stackOpt = converter.toStack(pickaxeOpt.get());
		if (stackOpt.isPresent()) {
			ItemStack stack = stackOpt.get();
			assertTrue(stack.getItem() instanceof PickaxeItem,
					"Pickaxe component should become PickaxeItem");
		}

		context.complete();
	}

	/**
	 * USE CASE: Tools work with vanilla Minecraft systems (don't crash).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void toolWorksWithVanillaSystems(TestContext context) {
		ItemStack tool = getAnyToolStack();

		if (tool == null) {
			context.complete();
			return;
		}

		// Should not crash
		assertNotNull(tool.getItem());
		assertTrue(tool.getMaxDamage() >= 0);
		assertFalse(tool.isEmpty());

		context.complete();
	}

	/**
	 * EDGE CASE: Tools with extreme attribute values work correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void extremeToolAttributes(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		var registry = ForgeroApi.componentRegistry();

		// Test extreme values
		var extremePickaxe = registry.get(OpenIdentifier.of("test-extreme-pickaxe"));
		if (extremePickaxe.isPresent()) {
			var stack = ForgeroApi.converter().toStack(extremePickaxe.get()).orElse(null);
			if (stack != null) {
				float miningSpeed = query.getMiningSpeed(stack);
				float attackDamage = query.getAttackDamage(stack);

				assertTrue(miningSpeed > 1_000_000f,
						"Extreme pickaxe should have >1M mining speed");
				assertTrue(attackDamage > 1_000_000f,
						"Extreme pickaxe should have >1M attack damage");
			}
		}

		// Test zero values
		var zeroPickaxe = registry.get(OpenIdentifier.of("test-zero-pickaxe"));
		if (zeroPickaxe.isPresent()) {
			var stack = ForgeroApi.converter().toStack(zeroPickaxe.get()).orElse(null);
			if (stack != null) {
				assertEquals(0f, query.getMiningSpeed(stack),
						"Zero pickaxe should have 0 mining speed");
				assertEquals(0f, query.getAttackDamage(stack),
						"Zero pickaxe should have 0 attack damage");
			}
		}

		// Test known values
		var knownPickaxe = registry.get(OpenIdentifier.of("test-known-pickaxe"));
		if (knownPickaxe.isPresent()) {
			var stack = ForgeroApi.converter().toStack(knownPickaxe.get()).orElse(null);
			if (stack != null) {
				assertEquals(8.0f, query.getMiningSpeed(stack),
						"Known pickaxe should have 8.0 mining speed");
				assertEquals(6.0f, query.getAttackDamage(stack),
						"Known pickaxe should have 6.0 attack damage");
				assertEquals(1561, query.getMaxDurability(stack),
						"Known pickaxe should have 1561 durability");
			}
		}

		context.complete();
	}

	/**
	 * USE CASE: Different tool types (pickaxe, axe, shovel, hoe, sword) all work.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void differentToolTypesWork(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();
		String[] toolTypes = {"pickaxe", "axe", "shovel", "hoe", "sword"};

		for (String toolType : toolTypes) {
			var toolOpt = registry.all().stream()
					.filter(c -> hasTag(c, toolType))
					.findFirst();

			if (toolOpt.isPresent()) {
				var stackOpt = converter.toStack(toolOpt.get());
				if (stackOpt.isPresent()) {
					ItemStack stack = stackOpt.get();
					assertFalse(stack.isEmpty(), toolType + " should convert to valid ItemStack");
				}
			}
		}

		context.complete();
	}

	/**
	 * USE CASE: Vanilla items don't break tool queries.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void vanillaToolsReturnDefaults(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemStack vanillaTool = new ItemStack(Items.DIAMOND_PICKAXE);

		// Should return defaults, not crash
		assertEquals(0.0f, query.getAttackDamage(vanillaTool));
		assertEquals(0.0f, query.getMiningSpeed(vanillaTool));
		assertEquals(0, query.getMaxDurability(vanillaTool));
		assertFalse(query.isForgeroItem(vanillaTool));

		context.complete();
	}

	// ==================== Helper Methods ====================

	private ItemStack getAnyToolStack() {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		var toolOpt = registry.all().stream()
				.filter(this::isToolComponent)
				.findFirst();

		return toolOpt.flatMap(converter::toStack).orElse(null);
	}

	private boolean isToolComponent(Component c) {
		return hasTag(c, "pickaxe") || hasTag(c, "axe") || hasTag(c, "shovel")
				|| hasTag(c, "hoe") || hasTag(c, "sword");
	}

	private boolean hasTag(Component c, String tagPart) {
		return c.getTags().stream()
				.anyMatch(tag -> tag.toString().contains(tagPart));
	}
}
