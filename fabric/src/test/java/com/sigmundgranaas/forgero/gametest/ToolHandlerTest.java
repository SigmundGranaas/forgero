package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;

import static com.sigmundgranaas.forgero.gametest.GameTestHelper.createDummyServerPlayer;

public class ToolHandlerTest {

    public static ItemStack createToolItemWithSchematic() {
        HeadState state = new HeadState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), null);
        ToolPartHead head = new PickaxeHead(state);
        HandleState handleState = new HandleState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), null);
        ToolPartHandle handle = new Handle(handleState);

        NbtCompound nbt = NBTFactory.INSTANCE.createNBTFromTool(ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle));

        ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxe")));
        stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, nbt);
        return stack;
    }

    public static ItemStack createToolItemWithVeinMining() {
        HeadState state = new HeadState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), null);
        ToolPartHead head = new PickaxeHead(state);
        HandleState handleState = new HandleState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), null);
        ToolPartHandle handle = new Handle(handleState);

        NbtCompound nbt = NBTFactory.INSTANCE.createNBTFromTool(ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle));

        ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxe")));
        stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, nbt);
        return stack;

    }

    @GameTest(templateName = "forgero:stone3x3", batchId = "Tool handlers")
    public void testToolHandler3x3(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        ItemStack baseTool = createToolItemWithSchematic();
        mockPlayer.setStackInHand(Hand.MAIN_HAND, baseTool);

        BlockPos targetPos = new BlockPos(1, 1, 0);
        BlockPos absolute = context.getAbsolutePos(targetPos);
        mockPlayer.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);
        mockPlayer.interactionManager.changeGameMode(GameMode.CREATIVE);


        boolean isDestroyed = true;


        ForgeroInitializer.LOGGER.info("Testing pattern breaking 3x3");

        //mockPlayer.interactionManager.tryBreakBlock(absolute);
        mockPlayer.interactionManager.processBlockBreakingAction(new BlockPos(absolute.getX(), absolute.getY() + 1, absolute.getZ()), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, Direction.DOWN, context.getWorld().getHeight(), 1);


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                BlockPos newPos = new BlockPos(absolute.getX() + i, absolute.getY() + j, absolute.getZ());
                BlockState currentState = context.getWorld().getBlockState(newPos);
                if (!currentState.isAir()) {
                    isDestroyed = false;
                }
            }
        }

        if (isDestroyed) {
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("Unable to break 3x3 pattern");
            throw new RuntimeException("test failed");
        }
    }

    @GameTest(templateName = "forgero:coal3x3", batchId = "Tool handlers")
    public void testVeinMining3x3Coal(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        ItemStack baseTool = createToolItemWithVeinMining();
        mockPlayer.setStackInHand(Hand.MAIN_HAND, baseTool);

        BlockPos targetPos = new BlockPos(1, 1, 0);
        BlockPos absolute = context.getAbsolutePos(targetPos);
        mockPlayer.teleport(context.getWorld(), absolute.getX(), absolute.getY() + 2, absolute.getZ(), 0, 0f);
        mockPlayer.interactionManager.changeGameMode(GameMode.CREATIVE);


        boolean isDestroyed = true;


        ForgeroInitializer.LOGGER.info("Testing breaking vein 3x3");

        //mockPlayer.interactionManager.tryBreakBlock(absolute);
        mockPlayer.interactionManager.processBlockBreakingAction(absolute, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, Direction.DOWN, context.getWorld().getHeight(), 1);


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                BlockPos newPos = new BlockPos(absolute.getX() + i, absolute.getY() + j, absolute.getZ());
                BlockState currentState = context.getWorld().getBlockState(newPos);
                if (!currentState.isAir()) {
                    isDestroyed = false;
                }
            }
        }

        if (isDestroyed) {
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("Unable to break 3x3 vein");
            throw new RuntimeException("test failed");
        }
    }
}
