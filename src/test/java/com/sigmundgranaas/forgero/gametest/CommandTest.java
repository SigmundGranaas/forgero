package com.sigmundgranaas.forgero.gametest;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CommandTest {
    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Command testing", required = true)
    public void TestCreateStationWithOp(TestContext context) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer-op");
        context.getWorld().getServer().getPlayerManager().addToOperators(profile);


        ServerPlayerEntity testPlayer = context.getWorld().getServer().getPlayerManager().createPlayer(profile);
        testPlayer.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.SERVERBOUND), testPlayer);

        BlockPos targetPos = new BlockPos(1, 5, 0);
        BlockPos absolute = context.getAbsolutePos(targetPos);
        testPlayer.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);

        int permissionLevel = context.getWorld().getServer().getPermissionLevel(profile);

        //Running gametest on server does not tackle permissions properly
        if (permissionLevel <= 1) {
            context.complete();
        } else {
            //testPlayer.sendMessage(new LiteralText("/forgero createstation"), MessageType.SYSTEM, testPlayer.getUuid());
            try {
                context.getWorld().getServer().getCommandManager().getDispatcher().execute("forgero createstation", testPlayer.getCommandSource());
                context.addFinalTask(() -> {
                    context.checkBlock(new BlockPos(3, 8, 0), block -> !block.getDefaultState().isAir(), "Forgero station is missing");
                });
            } catch (Exception e) {
                throw new GameTestException("Unable to create station: " + e.getMessage());
            }
        }

    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Command testing", required = true)
    public void failCreateStationWhenNotOp(TestContext context) {
        ServerPlayerEntity testPlayer = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer2"));
        testPlayer.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.SERVERBOUND), testPlayer);

        BlockPos targetPos = new BlockPos(1, 5, 0);
        BlockPos absolute = context.getAbsolutePos(targetPos);
        testPlayer.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);

        context.getWorld().getServer().getPlayerManager().removeFromOperators(testPlayer.getGameProfile());

        //testPlayer.sendMessage(new LiteralText("/forgero createstation"), MessageType.SYSTEM, testPlayer.getUuid());
        try {
            testPlayer.server.getCommandManager().getDispatcher().execute("forgero createstation", testPlayer.getCommandSource());
        } catch (Exception ignored) {

        }
        context.addFinalTask(() -> context.checkBlock(new BlockPos(3, 8, 0), block -> block.getDefaultState().isAir(), "Forgero station is missing"));
    }
}
