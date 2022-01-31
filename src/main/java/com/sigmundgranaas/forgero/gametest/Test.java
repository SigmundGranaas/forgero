package com.sigmundgranaas.forgero.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Method;

public class Test implements FabricGameTest {
    /**
     * By overriding invokeTestMethod you can wrap the method call.
     * This can be used as shown to run code before and after each test.
     */
    @Override
    public void invokeTestMethod(TestContext context, Method method) {
        beforeEach(context);

        FabricGameTest.super.invokeTestMethod(context, method);

        //afterEach(context);
    }

    private void beforeEach(TestContext context) {
        //System.out.println("Hello beforeEach");
        context.setBlockState(0, 5, 0, Blocks.GOLD_BLOCK);
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)
    public void noStructure(TestContext context) {
        context.setBlockState(0, 2, 0, Blocks.DIAMOND_BLOCK);


        context.addInstantFinalTask(() ->
                context.checkBlock(new BlockPos(0, 2, 0), (block) -> block == Blocks.DIAMOND_BLOCK, "Expect block to be diamond")
        );
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)
    public void noStructureTest2(TestContext context) {
        context.setBlockState(0, 2, 0, Blocks.DIAMOND_BLOCK);

        context.addInstantFinalTask(() ->
                context.checkBlock(new BlockPos(0, 1, 0), (block) -> block != Blocks.DIAMOND_BLOCK, "Expect block to be diamond")
        );
    }
}