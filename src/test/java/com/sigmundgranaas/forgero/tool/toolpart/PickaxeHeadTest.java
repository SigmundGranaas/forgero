package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.material.MaterialCollection;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PickaxeHeadTest {

    @Test
    void getToolTypeName() {
        ForgeroToolPart referenceToolPart = new PickaxeHead((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING)));
        Assertions.assertEquals(referenceToolPart.getToolTypeName(), "pickaxe");
    }

    @Test
    void getToolPartName() {
        ForgeroToolPart referenceToolPart = new PickaxeHead((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING)));
        Assertions.assertEquals(referenceToolPart.getToolPartName(), "pickaxehead");
    }
}