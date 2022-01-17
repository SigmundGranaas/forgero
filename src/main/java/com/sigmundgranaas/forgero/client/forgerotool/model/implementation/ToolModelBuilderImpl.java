package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModelBuilder;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.BakedToolModel;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public class ToolModelBuilderImpl implements ToolModelBuilder {

    private FabricBakedModel headModel = new EmptyBakedModel();
    private FabricBakedModel handleModel = new EmptyBakedModel();
    private FabricBakedModel bindingModel = new EmptyBakedModel();

    public FabricBakedModel getHeadModel() {
        return headModel;
    }

    public FabricBakedModel getHandleModel() {
        return handleModel;
    }

    public FabricBakedModel getBindingModel() {
        return bindingModel;
    }

    @Override
    public ToolModelBuilder addHead(FabricBakedModel head) {
        this.headModel = head;
        return this;
    }

    @Override
    public ToolModelBuilder addHandle(FabricBakedModel handle) {
        this.handleModel = handle;
        return this;
    }

    @Override
    public ToolModelBuilder addBinding(FabricBakedModel binding) {
        this.bindingModel = binding;
        return this;
    }

    @Override
    public BakedToolModel buildModel() {
        return new BakedToolModel(this);
    }
}
