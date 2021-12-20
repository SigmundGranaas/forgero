package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForgeroToolPartIdentifierImplTest {

    @Test
    void getMaterial() {
        ForgeroToolPartIdentifierImpl identifier = new ForgeroToolPartIdentifierImpl(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING);
        Assertions.assertEquals(identifier.getMaterial().getName(), Constants.FORGERO_MATERIAL_IDENTIFIER_STRING);
    }

    @Test
    void getToolPartType() {
        ForgeroToolPartIdentifierImpl identifier = new ForgeroToolPartIdentifierImpl(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING);
        Assertions.assertEquals(identifier.getToolPartType(), ForgeroToolPartTypes.PICKAXEHEAD);
    }
}