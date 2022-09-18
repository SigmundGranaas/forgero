package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolPartIdentifier;

import com.sigmundgranaas.forgero.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultForgeroToolTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }


    private static ForgeroToolPart createToolPart(String identifier) {
        return ForgeroToolPartFactory.INSTANCE.createToolPart((ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(identifier));
    }

    public static ForgeroToolBase getDefaultForgeroTool() {
        ToolPartHead head = (ToolPartHead) createToolPart(Constants.IRON_PICKAXEHEAD_IDENTIFIER);
        ToolPartHandle handle = (ToolPartHandle) createToolPart("oak-handle");
        return new ForgeroToolBase(head, handle);
    }

    @Test
    void getIdentifier() {
        assertEquals(new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER).getHead().getHeadType()
                , getDefaultForgeroTool().getToolHead().getToolType());
    }

    @Test
    void getShortToolName() {
        assertEquals("iron-pickaxe", getDefaultForgeroTool().getShortToolIdentifierString());
    }

    @Test
    void getToolName() {
        assertEquals(Constants.EXAMPLE_TOOL_IDENTIFIER, getDefaultForgeroTool().getToolIdentifierString());
    }

    @Test
    void getToolType() {
        assertEquals(ForgeroToolTypes.PICKAXE, getDefaultForgeroTool().getToolType());
    }
}