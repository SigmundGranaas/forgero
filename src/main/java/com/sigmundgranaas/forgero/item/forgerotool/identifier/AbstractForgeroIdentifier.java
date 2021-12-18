package com.sigmundgranaas.forgero.item.forgerotool.identifier;

public class AbstractForgeroIdentifier implements ForgeroIdentifier {
    private final ForgeroIdentifierType type;

    public AbstractForgeroIdentifier(ForgeroIdentifierType type) {
        this.type = type;
    }

    @Override
    public ForgeroIdentifierType getType() {
        return null;
    }
}
