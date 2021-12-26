package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartTypes getToolPartType();
}
