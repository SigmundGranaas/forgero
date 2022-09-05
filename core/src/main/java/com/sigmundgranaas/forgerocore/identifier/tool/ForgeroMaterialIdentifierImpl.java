package com.sigmundgranaas.forgerocore.identifier.tool;

import com.sigmundgranaas.forgerocore.identifier.ForgeroIdentifierType;

import java.util.Locale;

public class ForgeroMaterialIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroMaterialIdentifier {
    private final String name;

    public ForgeroMaterialIdentifierImpl(String materialName) {
        super(ForgeroIdentifierType.MATERIAL);
        this.name = materialName.toLowerCase(Locale.ROOT);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIdentifier() {
        return name;
    }
}
