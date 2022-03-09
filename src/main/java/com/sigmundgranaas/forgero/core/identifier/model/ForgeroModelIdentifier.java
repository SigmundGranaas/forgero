package com.sigmundgranaas.forgero.core.identifier.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifier;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;

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
        return String.format("%s_%s_%s_%s", material, type.toFileName(), layer.getFileName(), skin);
    }
}
