package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelBuilder;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.BakedToolPartModel;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public class ToolPartModelBuilderImpl implements ToolPartModelBuilder {
    private FabricBakedModel toolPartModel;
    private FabricBakedModel secondaryMaterial;
    private final FabricBakedModel gem;

    public ToolPartModelBuilderImpl() {
        this.toolPartModel = new EmptyBakedModel();
        this.secondaryMaterial = new EmptyBakedModel();
        this.gem = new EmptyBakedModel();
    }


    @Override
    public ToolPartModelBuilder addToolPart(FabricBakedModel toolPart) {
        this.toolPartModel = toolPart;
        return this;
    }

    @Override
    public ToolPartModelBuilder addSecondaryMaterial(FabricBakedModel secondaryMaterial) {
        this.secondaryMaterial = secondaryMaterial;
        return this;
    }

    @Override
    public ToolPartModelBuilder addGem() {
        return this;
    }

    public FabricBakedModel getSecondaryMaterial() {
        return secondaryMaterial;
    }

    public FabricBakedModel getGem() {
        return gem;
    }

    public FabricBakedModel getToolPartModel() {
        return toolPartModel;
    }

    @Override
    public FabricBakedModel build() {
        return new BakedToolPartModel(this);
    }
}
