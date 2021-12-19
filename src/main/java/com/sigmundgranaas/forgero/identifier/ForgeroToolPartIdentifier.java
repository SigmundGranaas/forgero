package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartTypes getToolPartType();
}
