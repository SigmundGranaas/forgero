package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.material.MaterialCollection;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HandleTest {

    @Test
    void getToolTypeName() {
        ForgeroToolPart referenceToolPart = new Handle((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING)));
        Assertions.assertEquals("handle", referenceToolPart.getToolTypeName());
    }

    @Test
    void getToolPartName() {
        ForgeroToolPart referenceToolPart = new Handle((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING)));
        Assertions.assertEquals("handle", referenceToolPart.getToolPartName());
    }
}