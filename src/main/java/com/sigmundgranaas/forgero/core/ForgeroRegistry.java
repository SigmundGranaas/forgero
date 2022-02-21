package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.gem.GemCollection;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollection;

public record ForgeroRegistry(MaterialCollection materialCollection,
                              GemCollection gemCollection,
                              ForgeroToolCollection toolCollection,
                              ForgeroToolPartCollection toolPartCollection) {
    private static ForgeroRegistry INSTANCE;

    public static ForgeroRegistry initializeRegistry(MaterialCollection materialCollection,
                                                     GemCollection gemCollection,
                                                     ForgeroToolCollection toolCollection,
                                                     ForgeroToolPartCollection toolPartCollection) {
        INSTANCE = new ForgeroRegistry(materialCollection,
                gemCollection,
                toolCollection,
                toolPartCollection);

        return INSTANCE;
    }

    public static ForgeroRegistry getInstance() {
        if (INSTANCE == null) {
            ForgeroResourceInitializer initializer = new ForgeroResourceInitializer();
            initializer.registerDefaultResources();
            INSTANCE = initializer.initializeForgeroResources();
        }
        return INSTANCE;
    }

    @Override
    public MaterialCollection materialCollection() {
        return materialCollection;
    }

    @Override
    public GemCollection gemCollection() {
        return gemCollection;
    }

    @Override
    public ForgeroToolCollection toolCollection() {
        return toolCollection;
    }

    @Override
    public ForgeroToolPartCollection toolPartCollection() {
        return toolPartCollection;
    }
}
