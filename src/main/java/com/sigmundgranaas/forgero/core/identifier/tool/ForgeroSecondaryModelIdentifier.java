package com.sigmundgranaas.forgero.core.identifier.tool;

public class ForgeroSecondaryModelIdentifier extends ForgeroModelIdentifier {
    public ForgeroSecondaryModelIdentifier(String identifier) {
        super(identifier);
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier() + "_secondary";
    }
}
