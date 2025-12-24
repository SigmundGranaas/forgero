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

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityTypeCondition(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();
		var zombie = context.spawnEntity(net.minecraft.entity.EntityType.ZOMBIE, context.getAbsolutePos(context.getRelativePos(net.minecraft.util.math.BlockPos.ORIGIN)));

		// Test player entity type
		var playerTypeCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.EntityTypeCondition(
				new OpenIdentifier("forgero", "entity_type"),
				new net.minecraft.util.Identifier("minecraft", "player")
		);

		DynamicContext playerContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, player)
				.build();

		context.assertTrue(playerTypeCondition.test(playerContext), "Should match player entity type");

		// Test zombie entity type
		var zombieTypeCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.EntityTypeCondition(
				new OpenIdentifier("forgero", "entity_type"),
				new net.minecraft.util.Identifier("minecraft", "zombie")
		);

		DynamicContext zombieContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.TARGET_ENTITY, zombie)
				.build();

		context.assertTrue(zombieTypeCondition.test(zombieContext), "Should match zombie entity type");

		// Test non-matching type
		context.assertFalse(playerTypeCondition.test(zombieContext), "Should not match wrong entity type");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityFlagCondition(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Test on_ground flag
		var onGroundCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.EntityFlagCondition(
				new OpenIdentifier("forgero", "entity_flag"),
				com.sigmundgranaas.forgero.properties.minecraft.condition.EntityFlagCondition.EntityFlag.ON_GROUND
		);

		DynamicContext groundContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, player)
				.build();

		context.assertTrue(onGroundCondition.test(groundContext), "Player should be on ground");

		// Test sprinting flag (player starts not sprinting)
		var sprintingCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.EntityFlagCondition(
				new OpenIdentifier("forgero", "entity_flag"),
				com.sigmundgranaas.forgero.properties.minecraft.condition.EntityFlagCondition.EntityFlag.SPRINTING
		);

		context.assertFalse(sprintingCondition.test(groundContext), "Player should not be sprinting initially");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockMatchCondition(TestContext context) {
		var pos = context.getAbsolutePos(context.getRelativePos(net.minecraft.util.math.BlockPos.ORIGIN));
		var world = context.getWorld();

		// Place a stone block
		world.setBlockState(pos, net.minecraft.block.Blocks.STONE.getDefaultState());

		// Test matching block
		var stoneCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.BlockMatchCondition(
				new OpenIdentifier("forgero", "block_match"),
				java.util.List.of(net.minecraft.block.Blocks.STONE)
		);

		DynamicContext blockContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, world)
				.put(MinecraftContextKeys.BLOCK_POS, pos)
				.build();

		context.assertTrue(stoneCondition.test(blockContext), "Should match stone block");

		// Test non-matching block
		var dirtCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.BlockMatchCondition(
				new OpenIdentifier("forgero", "block_match"),
				java.util.List.of(net.minecraft.block.Blocks.DIRT)
		);

		context.assertFalse(dirtCondition.test(blockContext), "Should not match dirt when stone is placed");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDimensionCondition(TestContext context) {
		var world = context.getWorld();
		var dimensionId = world.getRegistryKey().getValue();

		// Test matching dimension
		var matchingCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.DimensionCondition(
				new OpenIdentifier("forgero", "dimension"),
				dimensionId,
				true
		);

		DynamicContext dimContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, world)
				.build();

		context.assertTrue(matchingCondition.test(dimContext), "Should match current dimension");

		// Test inverted match (not in different dimension)
		var netherCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.DimensionCondition(
				new OpenIdentifier("forgero", "dimension"),
				new net.minecraft.util.Identifier("minecraft", "the_nether"),
				false  // NOT in nether
		);

		context.assertTrue(netherCondition.test(dimContext), "Should match 'not in nether' condition");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPositionCondition(TestContext context) {
		var pos = new net.minecraft.util.math.BlockPos(10, 64, 20);

		var exactCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.PositionCondition(
				new OpenIdentifier("forgero", "position"),
				10, 64, 20
		);

		DynamicContext posContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.BLOCK_POS, pos)
				.build();

		context.assertTrue(exactCondition.test(posContext), "Should match exact position");

		// Test non-matching position
		var wrongCondition = new com.sigmundgranaas.forgero.properties.minecraft.condition.PositionCondition(
				new OpenIdentifier("forgero", "position"),
				5, 64, 20
		);

		context.assertFalse(wrongCondition.test(posContext), "Should not match wrong position");

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
	 * NOT TESTED IN GAMETESTS:
	 * - WeatherCondition: Gametest environments don't reliably support weather manipulation
	 * - BiomeCondition: Would require complex biome setup in test world
	 * - BlockTagCondition: Tag matching works identically to BlockMatchCondition with tag lookup
	 */
}
