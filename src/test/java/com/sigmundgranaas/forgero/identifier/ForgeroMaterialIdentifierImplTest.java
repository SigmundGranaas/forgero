package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroMaterialIdentifierImplTest {

    @Test
    void getName() {
        ForgeroMaterialIdentifierImpl materialIdentifier = new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING);
        Assertions.assertEquals(Constants.IRON_IDENTIFIER_STRING, materialIdentifier.getName());
    }
}