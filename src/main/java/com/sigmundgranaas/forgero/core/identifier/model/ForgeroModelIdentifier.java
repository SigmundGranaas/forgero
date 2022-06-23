package com.sigmundgranaas.forgero.core.identifier.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

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
