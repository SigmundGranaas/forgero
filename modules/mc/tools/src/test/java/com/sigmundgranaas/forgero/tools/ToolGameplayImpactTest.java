package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.CombatTestHelper;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.DurabilityTestHelper;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.MiningTestHelper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Deep gameplay-impact tests for NATIVE Forgero tools (built as EquipmentComponents, distinct from
 * the vanilla-upgrades path). Where the existing tool tests assert {@code isSuitableFor} booleans or
 * apply {@code damage(max)} directly — and silently {@code complete()} when a tool is missing — these
 * drive the real game: break real blocks, hit a real mob, consume real durability. They fail fast if
 * a native tool is absent rather than passing vacuously.
 */
public class ToolGameplayImpactTest implements ForgeroGameTest {

	/** Resolves a native Forgero tool (e.g. {@code forgero:iron-pickaxe}); fails fast if absent. */
	private static ItemStack tool(String material, String type) {
		String id = "forgero:" + material + "-" + type;
		var comp = ForgeroApi.componentRegistry().get(OpenIdentifier.parse(id))
				.orElseThrow(() -> new IllegalStateException("Native Forgero tool not registered: " + id));
		return ForgeroApi.converter().toStack(comp)
				.orElseThrow(() -> new IllegalStateException("Could not convert to ItemStack: " + id));
	}

	/** A native iron pickaxe actually breaks and harvests stone. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_mines_and_harvests_stone(TestContext context) {
		ItemStack pick = tool("iron", "pickaxe");
		int ticks = MiningTestHelper.ticksToBreak(context, pick, Blocks.STONE);
		context.assertTrue(ticks > 0 && ticks < 200,
				"Native iron pickaxe should mine stone in a sane time, got " + ticks + " ticks");
		context.assertTrue(MiningTestHelper.harvestYieldsDrop(context, tool("iron", "pickaxe"), Blocks.STONE),
				"Native iron pickaxe should harvest stone (produce a drop)");
		context.complete();
	}

	/** Mining level gates harvesting: iron (lvl 2) harvests diamond ore; stone (lvl 1) does not. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mining_level_gates_diamond_ore_drops(TestContext context) {
		context.assertTrue(MiningTestHelper.harvestYieldsDrop(context, tool("iron", "pickaxe"), Blocks.DIAMOND_ORE),
				"Native iron pickaxe should harvest diamond ore");
		context.assertFalse(MiningTestHelper.harvestYieldsDrop(context, tool("stone", "pickaxe"), Blocks.DIAMOND_ORE),
				"Native stone pickaxe must NOT harvest diamond ore (mining level too low)");
		context.complete();
	}

	/**
	 * The damage a native sword actually deals to a mob matches the attack damage Forgero reports —
	 * i.e. the stat reaches combat, regardless of the exact balance in this content set.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_sword_real_damage_matches_reported(TestContext context) {
		ItemStack sword = tool("iron", "sword");
		float reported = ForgeroApi.itemQuery().getAttackDamage(sword);
		float dealt = CombatTestHelper.measureMeleeDamage(context, sword, EntityType.COW);
		context.assertTrue(reported > 1.0f, "Native iron sword should report real attack damage, got " + reported);
		context.assertTrue(Math.abs(dealt - reported) < 1.0f,
				"Native iron sword's real melee damage (" + dealt + ") must match its reported attack damage ("
						+ reported + "); the stat is not reaching combat.");
		context.complete();
	}

	/** A native iron pickaxe's durability is real: it survives close to its max number of breaks. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_durability_is_real(TestContext context) {
		int max = ForgeroApi.itemQuery().getMaxDurability(tool("iron", "pickaxe"));
		context.assertTrue(max > 1, "Native iron pickaxe should have a real max durability, got " + max);
		int target = Math.max(1, max - 5);
		context.assertTrue(DurabilityTestHelper.survivesBreaks(context, tool("iron", "pickaxe"), Blocks.STONE, target),
				"Native iron pickaxe should survive ~" + target + " real block-breaks");
		context.complete();
	}
}
