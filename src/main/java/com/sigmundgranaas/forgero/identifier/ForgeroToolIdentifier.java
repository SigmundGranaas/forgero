package com.sigmundgranaas.forgero.identifier;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();
}
