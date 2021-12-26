package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.HandleTest;
import com.sigmundgranaas.forgero.core.tool.toolpart.PickaxeHeadTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroToolBuilderImplTest {


    @Test
    void addGem() {
        //TODO()
    }

    @Test
    void addSkin() {
        //TODO()
    }

    @Test
    void createBaseTool() {
        ForgeroTool tool = ForgeroToolBuilder.createBuilder(PickaxeHeadTest.createDefaultPickaxeHead(), HandleTest.createDefaultToolPartHandle()).createTool();
        Assertions.assertEquals("iron_pickaxe_iron_pickaxehead_oak_handle", tool.getToolIdentifierString());
    }
}