package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;

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
