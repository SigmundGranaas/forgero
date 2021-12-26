package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HandleTest {
    public static Handle createDefaultToolPartHandle() {
        return new Handle((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.OAK_IDENTIFIER_STRING)));
    }


    @Test
    void getToolTypeName() {
        Assertions.assertEquals("handle", createDefaultToolPartHandle().getToolPartName());
    }

    @Test
    void getToolPartName() {
        ForgeroToolPart referenceToolPart = new Handle((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING)));
        Assertions.assertEquals("handle", referenceToolPart.getToolPartName());
    }
}