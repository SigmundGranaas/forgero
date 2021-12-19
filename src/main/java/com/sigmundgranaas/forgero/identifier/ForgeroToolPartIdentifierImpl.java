package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

public class ForgeroToolPartIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolPartIdentifier {
    private final String toolPartName;

    public ForgeroToolPartIdentifierImpl(ForgeroIdentifierType toolpart, String forgeroName) {
        super(toolpart);
        this.toolPartName = forgeroName;
    }

    @Override
    public ForgeroMaterialIdentifier getMaterial() {
        return (ForgeroMaterialIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolPartName.split("_")[0]);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.valueOf(toolPartName.split("_")[1].toUpperCase(Locale.ROOT));
    }
}
