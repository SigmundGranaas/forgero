package com.sigmundgranaas.forgero.item.forgerotool.identifier;

public class ForgeroMaterialIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroMaterialIdentifier {
    private final String name;

    public ForgeroMaterialIdentifierImpl(ForgeroIdentifierType type, String materialName) {
        super(type);
        this.name = materialName;
    }

    @Override
    public String getName() {
        return name;
    }
}
