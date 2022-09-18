package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroToolPartHeadIdentifier extends ForgeroToolPartIdentifierImpl {

    public ForgeroToolPartHeadIdentifier(String forgeroName) {
        super(forgeroName);
    }

    public ForgeroToolTypes getHeadType() {
        String headName = toolPartName.split(ELEMENT_SEPARATOR)[1];
        return ForgeroToolTypes.valueOf(headName.replace("head", "").toUpperCase(Locale.ROOT));
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }
}

