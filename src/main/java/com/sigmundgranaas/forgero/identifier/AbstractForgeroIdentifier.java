package com.sigmundgranaas.forgero.identifier;

public class AbstractForgeroIdentifier implements ForgeroIdentifier {
    private final ForgeroIdentifierType type;

    public AbstractForgeroIdentifier(ForgeroIdentifierType type) {
        this.type = type;
    }

    @Override
    public ForgeroIdentifierType getType() {
        return type;
    }
}
