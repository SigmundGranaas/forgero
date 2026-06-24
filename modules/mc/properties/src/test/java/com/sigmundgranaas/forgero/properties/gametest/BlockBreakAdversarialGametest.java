package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.CanMineFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Instant;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.RadiusVeinSelector;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * ADVERSARIAL tests for the AOE/vein block-breaking feature — written to find inconsistencies and
 * break it, not to confirm the happy path. Probes the selector logic directly (leak, boundary,
 * mining-level gating) and the real in-world break (vein breaks + non-matching spared + unbreakable
 * blocks survive), which the existing suite never exercises (it only checks select().size() / math).
 */
public class BlockBreakAdversarialGametest implements FabricGameTest {

	/** Places {@code block} along +Z from a relative root; returns the relative positions. */
	private static void line(TestContext ctx, Block block, int x, int y, int z0, int count) {
		for (int i = 0; i < count; i++) {
			ctx.setBlockState(new BlockPos(x, y, z0 + i), block);
		}
	}

	/** Same-block vein selection must NOT leak into an adjacent different block. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void same_block_vein_does_not_leak_into_other_blocks(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		line(context, Blocks.IRON_ORE, 2, 2, 2, 3);            // ore vein at z=2,3,4
		context.setBlockState(new BlockPos(2, 2, 5), Blocks.STONE); // stone touching the vein end

		Set<BlockPos> sel = new RadiusVeinSelector(10, new SameBlockFilter())
				.select(context.getAbsolutePos(new BlockPos(2, 2, 2)), player);

		context.assertTrue(sel.contains(context.getAbsolutePos(new BlockPos(2, 2, 4))),
				"vein must include the connected ore");
		context.assertFalse(sel.contains(context.getAbsolutePos(new BlockPos(2, 2, 5))),
				"same-block vein must NOT leak into the adjacent stone");
		context.complete();
	}

	/** Radius must bound the vein: radius 2 reaches 2 steps from root, not the 3rd/4th. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void radius_vein_respects_its_radius_boundary(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		line(context, Blocks.IRON_ORE, 2, 2, 2, 5);            // ore at z=2..6

		Set<BlockPos> sel = new RadiusVeinSelector(2, new SameBlockFilter())
				.select(context.getAbsolutePos(new BlockPos(2, 2, 2)), player);

		context.assertTrue(sel.contains(context.getAbsolutePos(new BlockPos(2, 2, 4))),
				"radius 2 must reach 2 steps from the root");
		context.assertFalse(sel.contains(context.getAbsolutePos(new BlockPos(2, 2, 6))),
				"radius 2 must NOT reach 4 steps from the root");
		context.complete();
	}

	/** Mining-level gate: a can-mine vein selects nothing the held tool cannot harvest. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void can_mine_vein_is_gated_by_mining_level(TestContext context) {
		line(context, Blocks.OBSIDIAN, 2, 2, 2, 3);
		BlockPos root = context.getAbsolutePos(new BlockPos(2, 2, 2));

		ServerPlayerEntity weak = context.createMockCreativeServerPlayerInWorld();
		weak.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_PICKAXE)); // cannot harvest obsidian
		Set<BlockPos> weakSel = new RadiusVeinSelector(10, new CanMineFilter()).select(root, weak);
		context.assertTrue(weakSel.isEmpty(),
				"a wooden pickaxe must vein-select NO obsidian (mining level too low), got " + weakSel.size());

		ServerPlayerEntity strong = context.createMockCreativeServerPlayerInWorld();
		strong.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE)); // can harvest obsidian
		Set<BlockPos> strongSel = new RadiusVeinSelector(10, new CanMineFilter()).select(root, strong);
		context.assertTrue(strongSel.size() >= 3,
				"a diamond pickaxe must vein-select the obsidian, got " + strongSel.size());
		context.complete();
	}

	/** Real mining: an AOE pickaxe breaks the whole same-block vein and spares the adjacent block. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void aoe_mining_breaks_vein_and_spares_adjacent(TestContext context) {
		ItemStack pick = ComponentTester.createStack("adv_aoe_pick", Set.of("pickaxe", "tool"),
				List.<Property>of(new BlockBreakingProperty(
						new RadiusVeinSelector(10, new SameBlockFilter()), new Instant(false), null)));

		line(context, Blocks.IRON_ORE, 2, 2, 2, 3);
		context.setBlockState(new BlockPos(2, 2, 5), Blocks.STONE);

		BlockBreakTestHelper.aoeMine(context, pick, new BlockPos(2, 2, 2));

		context.waitAndRun(2, () -> {
			context.assertTrue(context.getBlockState(new BlockPos(2, 2, 3)).isAir(), "vein block z=3 must be broken");
			context.assertTrue(context.getBlockState(new BlockPos(2, 2, 4)).isAir(), "vein block z=4 must be broken");
			context.assertFalse(context.getBlockState(new BlockPos(2, 2, 5)).isAir(),
					"the adjacent STONE must NOT be broken by a same-block ore vein");
			context.complete();
		});
	}
}
