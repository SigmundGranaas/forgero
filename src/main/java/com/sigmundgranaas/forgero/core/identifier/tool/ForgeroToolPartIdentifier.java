package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroPatternIdentifier;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroPatternIdentifier getPattern();

    ForgeroToolPartTypes getToolPartType();

}
