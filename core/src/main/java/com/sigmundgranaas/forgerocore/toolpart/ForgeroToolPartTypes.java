package com.sigmundgranaas.forgerocore.toolpart;

public enum ForgeroToolPartTypes {
    HEAD,
    HANDLE,
    BINDING;

    public String getName() {
        return this.name().toLowerCase();
    }
}
