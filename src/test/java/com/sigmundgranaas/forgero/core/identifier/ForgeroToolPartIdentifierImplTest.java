package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifierImpl;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroToolPartIdentifierImplTest {

    @Test
    void getMaterial() {
        ForgeroToolPartIdentifierImpl identifier = new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER);
        Assertions.assertEquals(identifier.getMaterial().getName(), Constants.IRON_IDENTIFIER_STRING);
    }

    @Test
    void getToolPartType() {
        ForgeroToolPartIdentifierImpl identifier = new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER);
        Assertions.assertEquals(identifier.getToolPartType(), ForgeroToolPartTypes.HEAD);
    }
}