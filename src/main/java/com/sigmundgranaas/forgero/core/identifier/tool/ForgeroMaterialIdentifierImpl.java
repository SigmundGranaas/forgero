package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;

public class ForgeroMaterialIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroMaterialIdentifier {
    private final String name;

    public ForgeroMaterialIdentifierImpl(String materialName) {
        super(ForgeroIdentifierType.MATERIAL);
        this.name = materialName;
    }

    @Override
    public String getName() {
        return name;
    }
}
