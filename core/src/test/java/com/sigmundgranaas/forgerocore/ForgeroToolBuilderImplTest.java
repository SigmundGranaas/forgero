package com.sigmundgranaas.forgerocore;


import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.factory.ForgeroToolBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForgeroToolBuilderImplTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

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
        Assertions.assertEquals("iron-pickaxe", tool.getToolIdentifierString());
    }
}