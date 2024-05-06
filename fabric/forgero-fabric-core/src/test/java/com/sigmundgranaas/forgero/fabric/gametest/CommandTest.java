package com.sigmundgranaas.forgero.fabric.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;


import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;


public class CommandTest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Command testing", required = true)
	public void TestCreateStationWithOp(TestContext context) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer-op");
		context.getWorld().getServer().getPlayerManager().addToOperators(profile);

		BlockPos targetPos = new BlockPos(1, 5, 0);

		ServerPlayerEntity entity = PlayerFactory.of(context, TestPos.of(targetPos, context));
		BlockPos absolute = context.getAbsolutePos(targetPos);
		entity.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);

		int permissionLevel = context.getWorld().getServer().getPermissionLevel(profile);

		//Running gametest on server does not tackle permissions properly
		if (permissionLevel <= 1) {
			context.complete();
		} else {
			//testPlayer.sendMessage(new LiteralText("/forgero createstation"), MessageType.SYSTEM, testPlayer.getUuid());
			try {
				context.getWorld().getServer().getCommandManager().getDispatcher().execute("forgero createstation", entity.getCommandSource());
				context.addFinalTask(() -> {
					context.checkBlock(new BlockPos(3, 8, 0), block -> !block.getDefaultState().isAir(), "Forgero station is missing");
				});
			} catch (Exception e) {
				throw new GameTestException("Unable to create station: " + e.getMessage());
			}
		}

	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Command testing", required = true)
	public void failCreateStationWhenNotOp(TestContext context) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer2");
		context.getWorld().getServer().getPlayerManager().addToOperators(profile);
		BlockPos targetPos = new BlockPos(1, 5, 0);

		ServerPlayerEntity entity = PlayerFactory.of(context, TestPos.of(targetPos, context));
		BlockPos absolute = context.getAbsolutePos(targetPos);
		entity.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);

		context.getWorld().getServer().getPlayerManager().removeFromOperators(entity.getGameProfile());

		//testPlayer.sendMessage(new LiteralText("/forgero createstation"), MessageType.SYSTEM, testPlayer.getUuid());
		try {
			entity.server.getCommandManager().getDispatcher().execute("forgero createstation", entity.getCommandSource());
		} catch (Exception ignored) {

		}
		context.addFinalTask(() -> context.checkBlock(new BlockPos(3, 8, 0), block -> block.getDefaultState().isAir(), "Forgero station is missing"));
	}
}
