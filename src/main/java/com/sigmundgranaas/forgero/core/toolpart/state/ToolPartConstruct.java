package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.Construct;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

public class ToolPartConstruct implements Construct<ForgeroToolPart> {
    private final ForgeroToolPart part;

    public ToolPartConstruct(ForgeroToolPart resource) {
        this.part = resource;
    }

    @Override
    public ForgeroToolPart getResource() {
        return part;
    }

    @Override
    public String getConstructIdentifier() {
        return part.getStringIdentifier();
    }
}
