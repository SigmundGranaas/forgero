package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifierImpl;
import com.sigmundgranaas.forgero.material.MaterialCollection;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ForgeroToolPartFactoryImplTest {

    @Test
    void testCreateToolPart() {
        ForgeroToolPart referenceToolPart = new PickaxeHead((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING)));

        ForgeroToolPartFactoryImpl factory = new ForgeroToolPartFactoryImpl();
        ForgeroToolPart part = factory.createToolPart(new ForgeroToolPartIdentifierImpl(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING));

        assertEquals(part.getToolPartName(), referenceToolPart.getToolPartName());
    }
}