package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;

public interface ForgeroToolPartFactory {
    static ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    ForgeroToolPart createToolPart(ForgeroToolPartIdentifier identifier);
}
