package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroIdentifierFactoryImplTest {


    @Test
    void testCreateMaterialIdentifier() {
        ForgeroMaterialIdentifierImpl materialIdentifierReference = new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING);

        ForgeroMaterialIdentifier materialIdentifier = (ForgeroMaterialIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING);
        Assertions.assertEquals(materialIdentifierReference.getName(), materialIdentifier.getName());

    }

    @Test
    void testCreateToolIdentifier() {
        ForgeroToolIdentifierImpl toolIdentifierReference = new ForgeroToolIdentifierImpl(Constants.FORGERO_TOOL_IDENTIFIER_STRING);

        ForgeroToolIdentifier toolIdentifier = (ForgeroToolIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.FORGERO_TOOL_IDENTIFIER_STRING);

        Assertions.assertEquals(toolIdentifierReference.getToolType(), toolIdentifier.getToolType());
    }

    @Test
    void testCreateToolPartIdentifier() {
        ForgeroToolPartIdentifierImpl toolPartIdentifierReference = new ForgeroToolPartIdentifierImpl(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING);

        ForgeroToolPartIdentifier toolPartIdentifier = (ForgeroToolPartIdentifier) new ForgeroIdentifierFactoryImpl().createForgeroIdentifier(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING);

        Assertions.assertEquals(toolPartIdentifierReference.getToolPartType(), toolPartIdentifier.getToolPartType());
    }
}