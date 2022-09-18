package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierType;

public abstract class AbstractForgeroIdentifier implements ForgeroIdentifier {
    private final ForgeroIdentifierType type;

    public AbstractForgeroIdentifier(ForgeroIdentifierType type) {
        this.type = type;
    }

    @Override
    public ForgeroIdentifierType getType() {
        return type;
    }
}
