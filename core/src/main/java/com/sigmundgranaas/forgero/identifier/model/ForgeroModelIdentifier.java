package com.sigmundgranaas.forgero.identifier.model;


import com.sigmundgranaas.forgero.deprecated.ModelLayer;
import com.sigmundgranaas.forgero.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierType;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

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
