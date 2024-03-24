package com.sigmundgranaas.forgero.testutil;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Direction;

public class PlayerActionHelper implements ContextSupplier {
	private final ContextSupplier supplier;
	private final ServerPlayerEntity player;

	public PlayerActionHelper(ContextSupplier supplier, ServerPlayerEntity player) {
		this.supplier = supplier;
		this.player = player;
	}

	public static PlayerActionHelper of(TestContext ctx, ServerPlayerEntity player) {
		return new PlayerActionHelper(ContextSupplier.of(ctx), player);
	}

	public static PlayerActionHelper of(ContextSupplier ctx, ServerPlayerEntity player) {
		return new PlayerActionHelper(ctx, player);
	}

	@Override
	public TestContext get() {
		return supplier.get();
	}

	public BlockState processBreakingBlock(TestPos pos, int ticks) {
		return processBreakingBlock(pos, ticks, Direction.EAST);
	}

	public BlockState processInstamine(TestPos pos) {
		return processInstamine(pos, Direction.EAST);
	}

	public BlockState processInstamine(TestPos pos, Direction dir) {
		player.interactionManager.processBlockBreakingAction(pos.absolute(), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, dir, get().getWorld().getHeight(), 1);
		player.interactionManager.update();
		player.interactionManager.processBlockBreakingAction(pos.absolute(), PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, dir, get().getWorld().getHeight(), 1);

		return absolute(pos);
	}

	public BlockState processBreakingBlock(TestPos pos, int ticks, Direction dir) {
		player.interactionManager.processBlockBreakingAction(pos.absolute(), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, dir, get().getWorld().getHeight(), 1);

		for (int i = 0; i < ticks; i++) {
			player.interactionManager.update();
		}

		player.interactionManager.processBlockBreakingAction(pos.absolute(), PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, dir, get().getWorld().getHeight(), 1);
		return absolute(pos);
	}

	public BlockState attemptBreakBlock(TestPos pos, Direction dir) {
		player.interactionManager.processBlockBreakingAction(pos.absolute(), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, dir, get().getWorld().getHeight(), 1);
		return absolute(pos);
	}

	public BlockState attemptBreakBlock(TestPos pos) {
		return attemptBreakBlock(pos, Direction.EAST);
	}
}
