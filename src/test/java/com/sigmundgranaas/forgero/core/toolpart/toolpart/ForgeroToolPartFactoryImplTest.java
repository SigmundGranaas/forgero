package com.sigmundgranaas.forgero.core.toolpart.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.core.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.property.ToolPropertyTest.PICKAXEHEAD_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ForgeroToolPartFactoryImplTest {

    @Test
    void testCreateToolPart() {
        HeadState state = new HeadState((PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING)), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), PICKAXEHEAD_PATTERN.get());
        ForgeroToolPart referenceToolPart = new PickaxeHead(state);

        ForgeroToolPartFactoryImpl factory = new ForgeroToolPartFactoryImpl();
        ForgeroToolPart part = factory.createToolPart(new ForgeroToolPartHeadIdentifier(Constants.IRON_PICKAXEHEAD_IDENTIFIER));

        assertEquals(part.getToolPartName(), referenceToolPart.getToolPartName());
    }
}