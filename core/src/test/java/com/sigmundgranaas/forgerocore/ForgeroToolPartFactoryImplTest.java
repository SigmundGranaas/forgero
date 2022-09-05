package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgerocore.toolpart.head.HeadState;
import com.sigmundgranaas.forgerocore.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgerocore.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;
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