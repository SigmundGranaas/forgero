package com.sigmundgranaas.forgerocore.identifier.tool;

import com.sigmundgranaas.forgerocore.identifier.ForgeroSchematicIdentifier;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;

public interface ForgeroToolPartIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroSchematicIdentifier getSchematic();

    ForgeroToolPartTypes getToolPartType();

}
