package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Verifies every head/blade tool gained a tip-reinforcement slot. Slot types aren't exposed through
 * the query API, so this checks that the basic wooden tools (which previously had only a binding
 * slot) now report two slots, and that a tip-capable material installs.
 */
public class TipSlotCoverageTest implements ForgeroGameTest {

	private static final Item[] BASIC_HEAD_TOOLS = {
			Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE
	};

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void basic_head_tools_have_binding_and_tip_slots(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		for (Item item : BASIC_HEAD_TOOLS) {
			int slots = query.getUpgradeSlotCount(new ItemStack(item));
			context.assertTrue(slots == 2,
					item + " should have binding + tip slots (2), got " + slots);
		}
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tip_material_installs_into_head_tool(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);
		ItemStack tip = ctx.toStack(ctx.component("forgero:blaze_rod").orElseThrow()).orElseThrow();

		context.assertTrue(mutate.canInstallUpgrade(pickaxe, tip),
				"A tip-reinforcement material (blaze rod) should install into a head tool");
		ItemStack upgraded = mutate.installUpgrade(pickaxe, tip);
		context.assertTrue(query.getFilledSlotCount(upgraded) == 1,
				"Tool should have one filled slot after installing a tip material, got " + query.getFilledSlotCount(upgraded));
		context.complete();
	}
}
