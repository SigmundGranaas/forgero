package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroSchematicIdentifier;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroSchematicIdentifier getSchematic();

    ForgeroToolPartTypes getToolPartType();

}
