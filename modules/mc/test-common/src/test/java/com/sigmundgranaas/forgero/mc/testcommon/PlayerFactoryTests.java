package com.sigmundgranaas.forgero.mc.testcommon;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

/**
 * GameTests for PlayerFactory builder.
 */
public class PlayerFactoryTests implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void simple_shouldCreatePlayer(TestContext context) {
        ServerPlayerEntity player = PlayerFactory.simple(context);

        context.assertTrue(player != null, "Player should be created");
        context.assertTrue(player.getPos() != null, "Player should have a position");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void withStack_shouldSetMainHandItem(TestContext context) {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        ServerPlayerEntity player = PlayerFactory.create(context)
                .withStack(stack)
                .build();

        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        context.assertTrue(mainHand.isOf(Items.DIAMOND_PICKAXE),
                "Player should hold diamond pickaxe");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void holding_shouldSetMainHandItem(TestContext context) {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        ServerPlayerEntity player = PlayerFactory.create(context)
                .holding(stack)
                .build();

        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        context.assertTrue(mainHand.isOf(Items.DIAMOND_SWORD),
                "Player should hold diamond sword");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void withOffHand_shouldSetOffHandItem(TestContext context) {
        ItemStack stack = new ItemStack(Items.SHIELD);
        ServerPlayerEntity player = PlayerFactory.create(context)
                .withOffHand(stack)
                .build();

        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        context.assertTrue(offHand.isOf(Items.SHIELD),
                "Player should hold shield in off hand");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void at_shouldSetPosition(TestContext context) {
        BlockPos targetPos = new BlockPos(1, 2, 3);
        ServerPlayerEntity player = PlayerFactory.create(context)
                .at(targetPos)
                .build();

        BlockPos absolutePos = context.getAbsolutePos(targetPos);
        double expectedX = absolutePos.getX() + 0.5;
        double expectedY = absolutePos.getY();
        double expectedZ = absolutePos.getZ() + 0.5;

        context.assertTrue(
                Math.abs(player.getX() - expectedX) < 0.1,
                "Player X position should match"
        );
        context.assertTrue(
                Math.abs(player.getY() - expectedY) < 0.1,
                "Player Y position should match"
        );
        context.assertTrue(
                Math.abs(player.getZ() - expectedZ) < 0.1,
                "Player Z position should match"
        );
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void facing_shouldSetYaw(TestContext context) {
        ServerPlayerEntity player = PlayerFactory.create(context)
                .facing(Direction.SOUTH)
                .build();

        float expectedYaw = Direction.SOUTH.asRotation();
        context.assertTrue(
                Math.abs(player.getYaw() - expectedYaw) < 0.1,
                "Player should face south"
        );
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void survival_shouldSetGameMode(TestContext context) {
        ServerPlayerEntity player = PlayerFactory.create(context)
                .survival()
                .build();

        context.assertTrue(
                player.interactionManager.getGameMode() == GameMode.SURVIVAL,
                "Player should be in survival mode"
        );
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void creative_shouldSetGameMode(TestContext context) {
        ServerPlayerEntity player = PlayerFactory.create(context)
                .creative()
                .build();

        context.assertTrue(
                player.interactionManager.getGameMode() == GameMode.CREATIVE,
                "Player should be in creative mode"
        );
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void builder_shouldChainMethods(TestContext context) {
        ItemStack mainHand = new ItemStack(Items.DIAMOND_AXE);
        ItemStack offHand = new ItemStack(Items.TORCH);

        ServerPlayerEntity player = PlayerFactory.create(context)
                .withName("test-builder")
                .at(1, 2, 3)
                .facing(Direction.EAST)
                .withStack(mainHand)
                .withOffHand(offHand)
                .survival()
                .build();

        context.assertTrue(player != null, "Player should be created");
        context.assertTrue(player.getName().getString().equals("test-builder"),
                "Player name should match");
        context.assertTrue(player.getStackInHand(Hand.MAIN_HAND).isOf(Items.DIAMOND_AXE),
                "Main hand should be diamond axe");
        context.assertTrue(player.getStackInHand(Hand.OFF_HAND).isOf(Items.TORCH),
                "Off hand should be torch");
        context.assertTrue(player.interactionManager.getGameMode() == GameMode.SURVIVAL,
                "Should be in survival mode");
        context.complete();
    }
}
