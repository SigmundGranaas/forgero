package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierType;
import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPartFactory;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultForgeroToolTest {

    private static ForgeroToolPart createToolPart(String identifier) {
        return ForgeroToolPartFactory.INSTANCE.createToolPart((ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(identifier));
    }

    public static DefaultForgeroTool getDefaultForgeroTool() {
        ForgeroToolPart head = createToolPart("iron_pickaxehead");
        ForgeroToolPart handle = createToolPart("oak_handle");
        return new DefaultForgeroTool(head, handle);
    }

    //TODO
    @Test
    void registerToolItem() {
    }

    @Test
    void getIdentifier() {
        assertEquals(new ForgeroToolIdentifierImpl(ForgeroIdentifierType.TOOL, "iron_pickaxe_iron_pickaxehead_oak_handle").getHead().getToolPartType().toString().toLowerCase(Locale.ROOT)
                , getDefaultForgeroTool().getToolHead().getToolPartName());
    }

    @Test
    void getShortToolName() {
        assertEquals("iron_pickaxe", getDefaultForgeroTool().getShortToolIdentifierString());
    }

    @Test
    void getToolName() {
        assertEquals("iron_pickaxe_iron_pickaxehead_oak_handle", getDefaultForgeroTool().getToolIdentifierString());
    }

    @Test
    void getToolType() {
        assertEquals(ForgeroToolTypes.PICKAXE, getDefaultForgeroTool().getToolType());
    }
}