package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroMaterialIdentifierImplTest {

    @Test
    void getName() {
        ForgeroMaterialIdentifierImpl materialIdentifier = new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING);
        Assertions.assertEquals(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING, materialIdentifier.getName());
    }
}