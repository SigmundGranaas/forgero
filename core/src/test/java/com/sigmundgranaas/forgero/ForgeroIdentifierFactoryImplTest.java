package com.sigmundgranaas.forgero;


import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactoryImpl;
import com.sigmundgranaas.forgero.identifier.tool.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroIdentifierFactoryImplTest {


    @Test
    void testCreateMaterialIdentifier() {
        ForgeroMaterialIdentifierImpl materialIdentifierReference = new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING);

        ForgeroMaterialIdentifier materialIdentifier = (ForgeroMaterialIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.IRON_IDENTIFIER_STRING);
        Assertions.assertEquals(materialIdentifierReference.getName(), materialIdentifier.getName());

    }

    @Test
    void testCreateToolIdentifier() {
        ForgeroToolIdentifierImpl toolIdentifierReference = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);

        ForgeroToolIdentifier toolIdentifier = (ForgeroToolIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.EXAMPLE_TOOL_IDENTIFIER);

        Assertions.assertEquals(toolIdentifierReference.getToolType(), toolIdentifier.getToolType());
    }

    @Test
    void testCreateToolPartIdentifier() {
        ForgeroToolPartIdentifierImpl toolPartIdentifierReference = new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER);

        ForgeroToolPartIdentifier toolPartIdentifier = (ForgeroToolPartIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.IRON_PICKAXEHEAD_IDENTIFIER);

        Assertions.assertEquals(toolPartIdentifierReference.getToolPartType(), toolPartIdentifier.getToolPartType());
    }
}