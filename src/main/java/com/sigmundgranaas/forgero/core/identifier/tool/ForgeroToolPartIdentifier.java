package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartTypes getToolPartType();
}
