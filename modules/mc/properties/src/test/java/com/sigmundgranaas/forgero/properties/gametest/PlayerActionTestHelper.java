package com.sigmundgranaas.forgero.properties.gametest;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlayerActionTestHelper {
	private final TestContext context;
	private final ServerPlayerEntity player;

	public PlayerActionTestHelper(TestContext context, ServerPlayerEntity player) {
		this.context = context;
		this.player = player;
	}

	/**
	 * Simulates a player instantly mining a block.
	 * This correctly sends the START and STOP packets to the interaction manager,
	 * which allows mixins listening to these events to trigger.
	 *
	 * @param pos The absolute position of the block to break.
	 */
	public void mineBlock(BlockPos pos) {
		// The player's interaction manager is what handles block breaking logic on the server.
		// We need to tell it that the player has started and immediately finished breaking a block.
		var interactionManager = player.interactionManager;
		var world = context.getWorld();

		// Simulate starting to break the block. The direction doesn't strictly matter for this test.
		interactionManager.processBlockBreakingAction(pos, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, Direction.UP, world.getHeight(), 1);

		// For an "instant" break, as defined by the property, we don't need to tick.
		// We can immediately send the stop action.
		interactionManager.processBlockBreakingAction(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, Direction.UP, world.getHeight(), 1);
	}
}
