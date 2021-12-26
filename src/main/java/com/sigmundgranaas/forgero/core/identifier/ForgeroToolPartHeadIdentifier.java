package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

public class ForgeroToolPartHeadIdentifier extends ForgeroToolPartIdentifierImpl {

    public ForgeroToolPartHeadIdentifier(String forgeroName) {
        super(forgeroName);
    }

    public ForgeroToolTypes getHeadType() {
        String headName = toolPartName.split("_")[1];
        return ForgeroToolTypes.valueOf(headName.replace("head", "").toUpperCase(Locale.ROOT));
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }
}

