package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelBuilder;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.BakedToolPartModel;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public class ToolPartModelBuilderImpl implements ToolPartModelBuilder {
    private FabricBakedModel gem;
    private FabricBakedModel toolPartModel;
    private FabricBakedModel secondaryMaterial;

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
    public ToolPartModelBuilder addGem(FabricBakedModel gem) {
        this.gem = gem;
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
