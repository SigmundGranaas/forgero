package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolModelBuilderImpl;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public interface ToolModelBuilder {
    static ToolModelBuilder createToolModelBuilder() {
        return new ToolModelBuilderImpl();
    }

    ToolModelBuilder addHead(FabricBakedModel head);

    ToolModelBuilder addHandle(FabricBakedModel handle);

    ToolModelBuilder addBinding(FabricBakedModel binding);

    FabricBakedModel getHeadModel();

    FabricBakedModel getHandleModel();

    FabricBakedModel getBindingModel();

    BakedToolModel buildModel();
}
