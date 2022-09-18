package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();

    ForgeroToolTypes getToolType();
}
