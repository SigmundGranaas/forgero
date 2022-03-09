package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.properties.ToolPropertyTest.IRON_PRIMARY;
import static com.sigmundgranaas.forgero.core.properties.ToolPropertyTest.OAK_PRIMARY;

public class IronToolPropertyTest {

    @Test
    void testToolDurability() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(IRON_PRIMARY.get(), ForgeroToolTypes.PICKAXE);
        //headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get());
        //handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertTrue(exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY) > 3000);
    }
}
