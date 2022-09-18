package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgero.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ForgeroToolPartFactoryImplTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    @Test
    void testCreateToolPart() {
        HeadState state = new HeadState(ForgeroRegistry.MATERIAL.getPrimaryMaterial(Constants.IRON_IDENTIFIER_STRING).get(), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), PICKAXEHEAD_SCHEMATIC.get());
        ForgeroToolPart referenceToolPart = new PickaxeHead(state);

        ForgeroToolPartFactoryImpl factory = new ForgeroToolPartFactoryImpl();
        ForgeroToolPart part = factory.createToolPart(new ForgeroToolPartHeadIdentifier(Constants.IRON_PICKAXEHEAD_IDENTIFIER));

        assertEquals(part.getToolPartName(), referenceToolPart.getToolPartName());
    }
}