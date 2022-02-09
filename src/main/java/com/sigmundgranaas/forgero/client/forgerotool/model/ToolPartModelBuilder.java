package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolPartModelBuilderImpl;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public interface ToolPartModelBuilder {
    static ToolPartModelBuilder createBuilder() {
        return new ToolPartModelBuilderImpl();
    }

    ToolPartModelBuilder addToolPart(FabricBakedModel toolPart);

    ToolPartModelBuilder addSecondaryMaterial(FabricBakedModel secondaryMaterial);

    ToolPartModelBuilder addGem(FabricBakedModel secondaryMaterial);

    FabricBakedModel build();

}