package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();

    ForgeroToolTypes getToolType();
}
