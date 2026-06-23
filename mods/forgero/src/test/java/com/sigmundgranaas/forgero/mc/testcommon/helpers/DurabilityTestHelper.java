package com.sigmundgranaas.forgero.mc.testcommon.helpers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

/**
 * Measures REAL durability consumption in a GameTest.
 * <p>
 * Instead of reading {@code getMaxDurability()}, this puts the tool in a survival player's hand and
 * actually breaks blocks with it via the interaction manager (which runs the vanilla post-mine
 * durability path), proving a durability upgrade lets a tool survive more real use — not just that a
 * number went up.
 */
public final class DurabilityTestHelper {

	private DurabilityTestHelper() {
	}

	/**
	 * Breaks copies of {@code block} with {@code tool} until the tool breaks or {@code cap} is reached.
	 *
	 * @return the number of blocks broken when the tool shattered, or {@code -1} if it survived all
	 *         {@code cap} breaks intact.
	 */
	public static int breaksToDestroy(TestContext context, ItemStack tool, Block block, int cap) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, tool);

		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));

		int broken = 0;
		while (broken < cap) {
			world.setBlockState(pos, block.getDefaultState());
			boolean ok = player.interactionManager.tryBreakBlock(pos);
			if (!ok) {
				world.removeBlock(pos, false);
				throw new GameTestException("breaksToDestroy: failed to break " + block + " at break #" + (broken + 1));
			}
			broken++;
			if (player.getMainHandStack().isEmpty()) {
				return broken; // tool shattered on this break
			}
		}
		world.removeBlock(pos, false);
		return -1; // survived `cap` breaks intact
	}

	/**
	 * Returns true if {@code tool} survives {@code count} real block-breaks without shattering.
	 */
	public static boolean survivesBreaks(TestContext context, ItemStack tool, Block block, int count) {
		return breaksToDestroy(context, tool, block, count) == -1;
	}

	/**
	 * Asserts {@code upgradedTool} outlasts {@code baseTool}: the base tool shatters within
	 * {@code breaks} real block-breaks, while the upgraded tool survives them all.
	 */
	public static void assertOutlasts(TestContext context, ItemStack baseTool, ItemStack upgradedTool, Block block,
			int breaks) {
		int baseLife = breaksToDestroy(context, baseTool, block, breaks);
		boolean upgradedSurvives = survivesBreaks(context, upgradedTool, block, breaks);
		if (baseLife == -1) {
			throw new GameTestException("assertOutlasts: base tool unexpectedly survived " + breaks
					+ " breaks; raise the break count above its base durability to expose the difference.");
		}
		if (!upgradedSurvives) {
			throw new GameTestException("Expected the durability upgrade to outlast the base tool: base shattered after "
					+ baseLife + " breaks and the upgraded tool also failed to survive " + breaks
					+ " breaks. The durability upgrade is not translating into real extra use.");
		}
	}
}
