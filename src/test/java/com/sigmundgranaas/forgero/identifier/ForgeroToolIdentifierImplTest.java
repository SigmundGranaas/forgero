package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroToolIdentifierImplTest {

    @Test
    void getMaterial() {
        ForgeroToolIdentifierImpl tool = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);
        Assertions.assertEquals(Constants.IRON_IDENTIFIER_STRING, tool.getMaterial().getName());
    }

    @Test
    void getToolType() {
        ForgeroToolIdentifierImpl tool = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);
        Assertions.assertEquals(ForgeroToolTypes.PICKAXE, tool.getToolType());
    }

    @Test
    void getHead() {
        ForgeroToolPartIdentifierImpl referenceIdentifier = new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER);

        ForgeroToolIdentifierImpl toolIdentifier = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);

        Assertions.assertEquals(referenceIdentifier.getToolPartType(), toolIdentifier.getHead().getToolPartType());
    }

    @Test
    void getHandle() {
        ForgeroToolPartIdentifierImpl referenceIdentifier = new ForgeroToolPartIdentifierImpl("oak_handle");

        ForgeroToolIdentifierImpl toolIdentifier = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);

        Assertions.assertEquals(referenceIdentifier.getToolPartType(), toolIdentifier.getHandle().getToolPartType());
    }
}