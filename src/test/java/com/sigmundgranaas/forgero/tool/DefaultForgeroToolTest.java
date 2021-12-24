package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultForgeroToolTest {

    private static ForgeroToolPart createToolPart(String identifier) {
        return ForgeroToolPartFactory.INSTANCE.createToolPart((ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(identifier));
    }

    public static ForgeroToolBase getDefaultForgeroTool() {
        ToolPartHead head = (ToolPartHead) createToolPart(Constants.IRON_PICKAXEHEAD_IDENTIFIER);
        ToolPartHandle handle = (ToolPartHandle) createToolPart("oak_handle");
        return new ForgeroToolBase(head, handle);
    }

    //TODO
    @Test
    void registerToolItem() {
    }

    @Test
    void getIdentifier() {
        assertEquals(new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER).getHead().getToolPartType().toString().toLowerCase(Locale.ROOT)
                , getDefaultForgeroTool().getToolHead().getToolPartName());
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