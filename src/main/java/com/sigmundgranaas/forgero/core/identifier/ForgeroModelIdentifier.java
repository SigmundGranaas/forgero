package com.sigmundgranaas.forgero.core.identifier;

public class ForgeroModelIdentifier {
    private final String identifier;

    public ForgeroModelIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getSecondaryMaterialIdentifier() {
        return identifier + "_secondary";
    }
}
