package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultForgeroToolTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new ForgeroResourceInitializer());
    }


    private static ForgeroToolPart createToolPart(String identifier) {
        return ForgeroToolPartFactory.INSTANCE.createToolPart((ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(identifier));
    }

    public static ForgeroToolBase getDefaultForgeroTool() {
        ToolPartHead head = (ToolPartHead) createToolPart(Constants.IRON_PICKAXEHEAD_IDENTIFIER);
        ToolPartHandle handle = (ToolPartHandle) createToolPart("oak_handle_default");
        return new ForgeroToolBase(head, handle);
    }

    @Test
    void getIdentifier() {
        assertEquals(new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER).getHead().getHeadType()
                , getDefaultForgeroTool().getToolHead().getToolType());
    }

    @Test
    void getShortToolName() {
        assertEquals("iron_pickaxe", getDefaultForgeroTool().getShortToolIdentifierString());
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