package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifierImpl;
import com.sigmundgranaas.forgero.core.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForgeroToolIdentifierImplTest {

    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

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
        ForgeroToolPartIdentifierImpl referenceIdentifier = new ForgeroToolPartIdentifierImpl("oak-handle");

        ForgeroToolIdentifierImpl toolIdentifier = new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER);

        Assertions.assertEquals(referenceIdentifier.getToolPartType(), toolIdentifier.getHandle().getToolPartType());
    }
}