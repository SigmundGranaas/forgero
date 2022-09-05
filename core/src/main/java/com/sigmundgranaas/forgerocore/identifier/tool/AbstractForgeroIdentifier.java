package com.sigmundgranaas.forgerocore.identifier.tool;

import com.sigmundgranaas.forgerocore.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgerocore.identifier.ForgeroIdentifierType;

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
