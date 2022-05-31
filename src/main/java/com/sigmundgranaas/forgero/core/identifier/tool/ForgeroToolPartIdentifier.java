package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroSchematicIdentifier;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroSchematicIdentifier getSchematic();

    ForgeroToolPartTypes getToolPartType();

}
