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

    public static DefaultForgeroTool getDefaultForgeroTool() {
        ToolPartHead head = (ToolPartHead) createToolPart(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING);
        ToolPartHandle handle = (ToolPartHandle) createToolPart("oak_handle");
        return new DefaultForgeroTool(head, handle);
    }

    //TODO
    @Test
    void registerToolItem() {
    }

    @Test
    void getIdentifier() {
        assertEquals(new ForgeroToolIdentifierImpl(Constants.FORGERO_TOOL_IDENTIFIER_STRING).getHead().getToolPartType().toString().toLowerCase(Locale.ROOT)
                , getDefaultForgeroTool().getToolHead().getToolPartName());
    }

    @Test
    void getShortToolName() {
        assertEquals("iron_pickaxe", getDefaultForgeroTool().getShortToolIdentifierString());
    }

    @Test
    void getToolName() {
        assertEquals(Constants.FORGERO_TOOL_IDENTIFIER_STRING, getDefaultForgeroTool().getToolIdentifierString());
    }

    @Test
    void getToolType() {
        assertEquals(ForgeroToolTypes.PICKAXE, getDefaultForgeroTool().getToolType());
    }
}