package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.gem.GemCollection;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.schematic.SchematicCollection;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollection;

public record LegacyForgeroRegistry(MaterialCollection materialCollection,
                                    GemCollection gemCollection,
                                    ForgeroToolCollection toolCollection,
                                    ForgeroToolPartCollection toolPartCollection,
                                    SchematicCollection schematicCollection) {
    private static LegacyForgeroRegistry INSTANCE;

    public static LegacyForgeroRegistry initializeRegistry(MaterialCollection materialCollection,
                                                           GemCollection gemCollection,
                                                           ForgeroToolCollection toolCollection,
                                                           ForgeroToolPartCollection toolPartCollection, SchematicCollection patternCollection) {
        INSTANCE = new LegacyForgeroRegistry(materialCollection,
                gemCollection,
                toolCollection,
                toolPartCollection,
                patternCollection);

        return INSTANCE;
    }

    public static LegacyForgeroRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroResourceInitializer().initializeForgeroResources();
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

    public SchematicCollection schematicCollection() {
        return schematicCollection;
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
