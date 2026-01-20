package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-value gameplay tests for the Golden Feature.
 * Validates that Forgero gold tools pacify Piglins when HELD (not worn).
 *
 * <p>VALUE: ⭐⭐⭐⭐⭐ This is critical for Nether gameplay - players MUST be able to
 * use gold Forgero tools safely around Piglins.
 *
 * <p>IMPORTANT: This is NEW functionality that vanilla doesn't have!
 * - Vanilla: Only gold ARMOR pacifies Piglins (checked in armor slots)
 * - Forgero: Gold tools HELD in hand also pacify Piglins (via PiglinBrainMixin)
 *
 * <p>The golden feature is implemented via PiglinBrainMixin which injects into
 * wearsGoldArmor() to also check held items for the "forgero:golden" feature.
 */
public class GoldenFeatureTest implements ForgeroGameTest {

	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	/**
	 * GAMEPLAY TEST: Player holding gold Forgero pickaxe pacifies Piglins.
	 *
	 * <p>This is NEW functionality - vanilla gold pickaxes in hand don't pacify Piglins,
	 * but Forgero gold tools do (via the PiglinBrainMixin).
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ Core Nether gameplay enhancement.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_gold_pickaxe_pacifies_piglins(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var goldPickaxe = ctx.component("forgero:gold-pickaxe");
		assertTrue(goldPickaxe.isPresent(), "Gold pickaxe must be registered");
		ItemStack goldStack = ctx.toStack(goldPickaxe.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(goldStack)
				.build();

		assertTrue(PiglinBrain.wearsGoldArmor(player),
				"Player holding gold Forgero pickaxe must pacify Piglins (via PiglinBrainMixin)");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding gold Forgero sword pacifies Piglins.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ Validates golden feature works across tool types.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_gold_sword_pacifies_piglins(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var goldSword = ctx.component("forgero:gold-sword");
		assertTrue(goldSword.isPresent(), "Gold sword must be registered");
		ItemStack goldStack = ctx.toStack(goldSword.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(goldStack)
				.build();

		assertTrue(PiglinBrain.wearsGoldArmor(player),
				"Player holding gold Forgero sword must pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding gold Forgero axe pacifies Piglins.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ Validates golden feature works for axes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_gold_axe_pacifies_piglins(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var goldAxe = ctx.component("forgero:gold-axe");
		assertTrue(goldAxe.isPresent(), "Gold axe must be registered");
		ItemStack goldStack = ctx.toStack(goldAxe.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(goldStack)
				.build();

		assertTrue(PiglinBrain.wearsGoldArmor(player),
				"Player holding gold Forgero axe must pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding gold Forgero tool in offhand pacifies Piglins.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Players often hold tools in offhand - this must work too.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_gold_tool_offhand_pacifies_piglins(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var goldSword = ctx.component("forgero:gold-sword");
		assertTrue(goldSword.isPresent(), "Gold sword must be registered");
		ItemStack goldStack = ctx.toStack(goldSword.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.withOffHand(goldStack)
				.build();

		assertTrue(PiglinBrain.wearsGoldArmor(player),
				"Player holding gold Forgero tool in offhand must pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding iron Forgero tool does NOT pacify Piglins.
	 *
	 * <p>Validates that only gold tools trigger the golden behavior.
	 * Iron tools should NOT provide Piglin protection.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Negative test - ensures only gold tools provide protection.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_iron_tool_no_protection(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var ironPickaxe = ctx.component("forgero:iron-pickaxe");
		assertTrue(ironPickaxe.isPresent(), "Iron pickaxe must be registered");
		ItemStack ironStack = ctx.toStack(ironPickaxe.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(ironStack)
				.build();

		assertFalse(PiglinBrain.wearsGoldArmor(player),
				"Player holding iron Forgero tool must NOT pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding diamond Forgero tool does NOT pacify Piglins.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Ensures diamond tools don't accidentally trigger golden behavior.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void player_holding_diamond_tool_no_protection(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var diamondSword = ctx.component("forgero:diamond-sword");
		assertTrue(diamondSword.isPresent(), "Diamond sword must be registered");
		ItemStack diamondStack = ctx.toStack(diamondSword.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(diamondStack)
				.build();

		assertFalse(PiglinBrain.wearsGoldArmor(player),
				"Player holding diamond Forgero tool must NOT pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Empty-handed player does NOT pacify Piglins (baseline).
	 *
	 * <p>VALUE: ⭐⭐⭐ Baseline test - ensures our mixin doesn't accidentally pacify all players.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void empty_handed_player_no_protection(TestContext context) {
		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.build();

		assertFalse(PiglinBrain.wearsGoldArmor(player),
				"Empty-handed player must NOT pacify Piglins");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: Player holding vanilla iron pickaxe does NOT pacify Piglins.
	 *
	 * <p>This verifies the mixin doesn't accidentally make all held items golden.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Regression test - ensures mixin scope is correct.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "golden_feature")
	public void vanilla_iron_pickaxe_no_protection(TestContext context) {
		ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(ironPickaxe)
				.build();

		assertFalse(PiglinBrain.wearsGoldArmor(player),
				"Vanilla iron pickaxe in hand must NOT pacify Piglins");

		context.complete();
	}
}
