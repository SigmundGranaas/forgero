package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgerocore.ToolPropertyTest.*;

public class IronToolPropertyTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    @Test
    void testToolDurability() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(IRON_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        //headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        //handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertTrue(exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY) > 250);
    }

    @Test
    void testToolDurabilityWithSecondary() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(IRON_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(1200, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY), 200);
    }

    @Test
    void testToolMiningLevel() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(IRON_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        //headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        //handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(2, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_LEVEL));
    }

    @Test
    void testToolMiningSpeed() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(IRON_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        //headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        //handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(7, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_SPEED), 1);
    }
}
