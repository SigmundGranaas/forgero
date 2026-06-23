package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.MiningTestHelper;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Deep, real-gameplay validation of material-tier effectiveness for native Forgero tools.
 * <p>
 * The sibling {@code UpgradeEffectivenessTest} asserts "diamond is better than iron" by comparing
 * raw attribute numbers. These tests prove it in play: a diamond pickaxe breaks stone in fewer real
 * ticks than iron, and mining level gates real drops (diamond harvests obsidian; iron does not).
 * Run in the full-content forgero runtime where every native tier exists; fails fast if one is absent.
 */
public class ToolTierEffectivenessTest implements ForgeroGameTest {

	private static ItemStack tool(String material, String type) {
		String id = "forgero:" + material + "-" + type;
		var comp = ForgeroApi.componentRegistry().get(OpenIdentifier.parse(id))
				.orElseThrow(() -> new IllegalStateException("Native Forgero tool not registered: " + id));
		return ForgeroApi.converter().toStack(comp)
				.orElseThrow(() -> new IllegalStateException("Could not convert to ItemStack: " + id));
	}

	/** A diamond pickaxe actually breaks stone in fewer ticks than an iron one. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_pickaxe_mines_stone_faster_than_iron(TestContext context) {
		MiningTestHelper.assertBreaksFaster(context, tool("iron", "pickaxe"), tool("diamond", "pickaxe"), Blocks.STONE);
		context.complete();
	}

	/** Mining level gates real obsidian drops: diamond (lvl 3) harvests it, iron (lvl 2) does not. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mining_level_gates_obsidian_drops(TestContext context) {
		context.assertTrue(MiningTestHelper.harvestYieldsDrop(context, tool("diamond", "pickaxe"), Blocks.OBSIDIAN),
				"Native diamond pickaxe should harvest obsidian");
		context.assertFalse(MiningTestHelper.harvestYieldsDrop(context, tool("iron", "pickaxe"), Blocks.OBSIDIAN),
				"Native iron pickaxe must NOT harvest obsidian (mining level too low)");
		context.complete();
	}
}
