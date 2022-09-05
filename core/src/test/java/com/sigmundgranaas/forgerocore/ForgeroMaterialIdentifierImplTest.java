package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroMaterialIdentifierImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroMaterialIdentifierImplTest {

    @Test
    void getName() {
        ForgeroMaterialIdentifierImpl materialIdentifier = new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING);
        Assertions.assertEquals(Constants.IRON_IDENTIFIER_STRING, materialIdentifier.getName());
    }
}