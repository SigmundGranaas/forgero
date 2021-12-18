package com.sigmundgranaas.forgero.item.forgerotool.identifier;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();
}
