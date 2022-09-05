package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolPartModelBuilderImpl;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public interface ToolPartModelBuilder {
    static ToolPartModelBuilder createBuilder() {
        return new ToolPartModelBuilderImpl();
    }

    @SuppressWarnings("UnusedReturnValue")
    ToolPartModelBuilder addToolPart(FabricBakedModel toolPart);

    @SuppressWarnings("UnusedReturnValue")
    ToolPartModelBuilder addSecondaryMaterial(FabricBakedModel secondaryMaterial);

    @SuppressWarnings("UnusedReturnValue")
    ToolPartModelBuilder addGem(FabricBakedModel secondaryMaterial);

    FabricBakedModel build();

}