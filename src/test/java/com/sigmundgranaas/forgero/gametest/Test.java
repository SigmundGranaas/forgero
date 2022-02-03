package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.ToolMaterialAdapter;
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

    // GameTests won't run unless there is an added task
    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)
    public void randomTest(TestContext context) {
        context.addInstantFinalTask(() -> {
            assert (createDummyTool().getDurability() > 0);
        });
    }

    private ForgeroTool createDummyTool() {
        PrimaryMaterial material = new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO());
        ToolMaterialAdapter adapter = new ToolMaterialAdapter(material);
        ToolPartHead head = ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(material, ForgeroToolTypes.PICKAXE).createToolPart();
        ToolPartHandle handle = ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(material).createToolPart();

        return ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle);
    }
}