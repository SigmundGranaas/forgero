package com.sigmundgranaas.forgerocore.identifier.model;


import com.sigmundgranaas.forgerocore.deprecated.ModelLayer;
import com.sigmundgranaas.forgerocore.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgerocore.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgerocore.identifier.ForgeroIdentifierType;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

public record ForgeroModelIdentifier(
        String material,
        ToolPartModelType type,
        ModelLayer layer,
        String skin) implements ForgeroIdentifier {

    @Override
    public ForgeroIdentifierType getType() {
        return ForgeroIdentifierType.MODEL;
    }

    @Override
    public String getIdentifier() {
        return String.format("%s%s%s%s%s%s%s", material, ELEMENT_SEPARATOR, type.toFileName(), ELEMENT_SEPARATOR, layer.getFileName(), ELEMENT_SEPARATOR, skin);
    }
}
