package com.sigmundgranaas.forgero.core.toolpart;

public enum ForgeroToolPartTypes {
    HEAD,
    HANDLE,
    BINDING;

    public String getName() {
        return this.name().toLowerCase();
    }
}
