package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ForgeroToolPartFactoryImplTest {

    @Test
    void testCreateToolPart() {
        ForgeroToolPart referenceToolPart = new PickaxeHead((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING)));

        ForgeroToolPartFactoryImpl factory = new ForgeroToolPartFactoryImpl();
        ForgeroToolPart part = factory.createToolPart(new ForgeroToolPartHeadIdentifier(Constants.IRON_PICKAXEHEAD_IDENTIFIER));

        assertEquals(part.getToolPartName(), referenceToolPart.getToolPartName());
    }
}