package com.sigmundgranaas.forgero.mc.testcommon.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

/**
 * Measures REAL mining outcomes in a GameTest.
 * <p>
 * Rather than reading a mining-speed number off an item, this uses the same formula vanilla uses to
 * advance block-breaking progress ({@link BlockState#calcBlockBreakingDelta}) to report how many
 * ticks a tool actually needs to break a block, and uses {@link Block#getDroppedStacks} (tool-aware)
 * to report whether a tool's mining level is high enough to actually harvest a block. Both reflect
 * gameplay (a faster tool breaks sooner; an under-levelled tool drops nothing), not paper stats.
 */
public final class MiningTestHelper {

	private MiningTestHelper() {
	}

	/**
	 * Returns the number of ticks {@code tool} needs to break {@code block} — the real in-game break
	 * duration, accounting for mining speed, block hardness and tool suitability.
	 * Fails fast if the tool makes no progress at all.
	 */
	public static int ticksToBreak(TestContext context, ItemStack tool, Block block) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, tool);

		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
		world.setBlockState(pos, block.getDefaultState());
		BlockState state = world.getBlockState(pos);

		float delta = state.calcBlockBreakingDelta(player, world, pos);
		world.removeBlock(pos, false);
		if (delta <= 0f) {
			throw new GameTestException("ticksToBreak: tool makes no progress on " + block + " (delta=" + delta + ")");
		}
		// Vanilla adds `delta` to break progress each tick; the block pops once progress >= 1.0.
		return (int) Math.ceil(1.0f / delta);
	}

	/**
	 * Returns true if {@code tool}'s mining level is high enough to actually harvest {@code block} —
	 * i.e. breaking it in survival through the interaction manager spawns a drop. This exercises the
	 * real {@code canHarvest}/{@code isSuitableFor} gate (a wooden pickaxe breaks iron ore but drops
	 * nothing), not just the block's loot table. Returns false if the tool is too low a tier.
	 */
	public static boolean harvestYieldsDrop(TestContext context, ItemStack tool, Block block) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, tool);

		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
		world.setBlockState(pos, block.getDefaultState());

		Box area = new Box(pos).expand(2.0);
		world.getEntitiesByClass(ItemEntity.class, area, e -> true).forEach(ItemEntity::discard); // clear strays
		player.interactionManager.tryBreakBlock(pos);
		boolean dropped = !world.getEntitiesByClass(ItemEntity.class, area, e -> true).isEmpty();

		world.getEntitiesByClass(ItemEntity.class, area, e -> true).forEach(ItemEntity::discard);
		world.removeBlock(pos, false);
		return dropped;
	}

	/**
	 * Asserts {@code fastTool} breaks {@code block} in strictly fewer ticks than {@code baseTool}.
	 */
	public static void assertBreaksFaster(TestContext context, ItemStack baseTool, ItemStack fastTool, Block block) {
		int base = ticksToBreak(context, baseTool, block);
		int fast = ticksToBreak(context, fastTool, block);
		if (fast >= base) {
			throw new GameTestException("Expected the upgraded tool to break " + block
					+ " faster, but base=" + base + " ticks, upgraded=" + fast + " ticks. "
					+ "The mining-speed upgrade is not affecting real break time.");
		}
	}
}
