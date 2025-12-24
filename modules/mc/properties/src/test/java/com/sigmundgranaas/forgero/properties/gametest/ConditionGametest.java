package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.properties.minecraft.condition.DamagePercentageCondition;
import com.sigmundgranaas.forgero.properties.minecraft.condition.MinecraftContextKeys;
import com.sigmundgranaas.forgero.properties.minecraft.condition.RandomCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.Key;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.*;

/**
 * Gametests for dynamic conditions that can be tested at runtime.
 *
 * Note: Static conditions (IdMatchCondition, AtDepthCondition, etc.) are evaluated
 * at component bake-time and operate on component tree structure. They are tested
 * indirectly through the property system and data-driven JSON configuration.
 *
 * Weather conditions are not tested here because gametest environments don't
 * reliably support weather manipulation.
 */
public class ConditionGametest {

	// ========== DYNAMIC CONDITION TESTS ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDamagePercentageCondition(TestContext context) {
		ItemStack stack = new ItemStack(Items.IRON_PICKAXE);

		// Create context with undamaged item
		DynamicContext undamagedContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.STACK, stack)
				.build();

		// Test 0% damage (undamaged)
		var noDamage = new DamagePercentageCondition(
				new OpenIdentifier("forgero", "damage_percentage"),
				0.0f
		);
		context.assertTrue(noDamage.test(undamagedContext), "Undamaged item should match 0% damage");

		// Damage the item to 50%
		int maxDamage = stack.getMaxDamage();
		stack.setDamage(maxDamage / 2);

		DynamicContext damagedContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.STACK, stack)
				.build();

		// Test 50% damage
		var halfDamage = new DamagePercentageCondition(
				new OpenIdentifier("forgero", "damage_percentage"),
				0.5f
		);
		context.assertTrue(halfDamage.test(damagedContext), "Half-damaged item should match 50% damage");

		// Test with 0-100 scale
		var halfDamage100Scale = new DamagePercentageCondition(
				new OpenIdentifier("forgero", "damage_percentage"),
				50.0f
		);
		context.assertTrue(halfDamage100Scale.test(damagedContext), "Should support 0-100 scale for damage percentage");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRandomConditionDeterminism(TestContext context) {
		// Test with deterministic seed (no seed sources)
		var always = new RandomCondition(
				new OpenIdentifier("forgero", "random"),
				1.0f,
				0,
				List.of()
		);

		var never = new RandomCondition(
				new OpenIdentifier("forgero", "random"),
				0.0f,
				0,
				List.of()
		);

		DynamicContext emptyContext = DynamicContext.empty();

		// 100% chance should always pass
		context.assertTrue(always.test(emptyContext), "1.0 probability should always return true");

		// 0% chance should always fail
		context.assertFalse(never.test(emptyContext), "0.0 probability should always return false");

		// Test with 50% chance - result should be deterministic for same seed
		var fifty = new RandomCondition(
				new OpenIdentifier("forgero", "random"),
				0.5f,
				0,
				List.of()
		);

		boolean firstResult = fifty.test(emptyContext);
		boolean secondResult = fifty.test(emptyContext);

		context.assertTrue(firstResult == secondResult, "Random condition with no seed sources should be deterministic");

		context.complete();
	}

	// ========== NOTES ON UNTESTED CONDITIONS ==========

	/*
	 * STATIC CONDITIONS:
	 * Static conditions (IdMatchCondition, AtDepthCondition, InSlotTypeCondition, etc.)
	 * are evaluated at component bake-time when the JSON data is processed into component trees.
	 * They operate on ResolutionContext which requires complex component tree setup.
	 * These are tested indirectly through:
	 * - Integration tests with real JSON data
	 * - Property system tests
	 * - End-to-end item creation tests
	 *
	 * WEATHER CONDITION:
	 * WeatherCondition is NOT tested here because:
	 * 1. Gametests run in isolated test worlds
	 * 2. Controlling weather in gametests is unreliable
	 * 3. Weather state may not persist properly in test contexts
	 * 4. The condition is simple enough that manual testing is sufficient
	 */
}
