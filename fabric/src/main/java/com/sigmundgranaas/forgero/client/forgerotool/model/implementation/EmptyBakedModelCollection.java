package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.client.forgerotool.model.BakedModelCollection;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroAdapter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EmptyBakedModelCollection implements BakedModelCollection {
    private final FabricToForgeroToolAdapter toolAdapter;

    public EmptyBakedModelCollection() {
        this.toolAdapter = new FabricToForgeroAdapter();
    }

    @Override
    public Map<String, FabricBakedModel> getBakedToolPartModels() {
        return new HashMap<>();
    }

    @Override
    public FabricBakedModel getModel(String identifier) {
        return getEmptyModel(identifier);
    }

    @Override
    public FabricBakedModel getToolModel(ItemStack forgeroToolStack) {
        return toolAdapter.getTool(forgeroToolStack).map(tool -> getEmptyModel(tool.getToolIdentifierString())).orElse(getEmptyModel("Unknown model"));
    }

    @Override
    public FabricBakedModel getToolPartModel(ItemStack ForgeroToolPartModel) {
        return toolAdapter.getTool(ForgeroToolPartModel).map(tool -> getEmptyModel(tool.getToolIdentifierString())).orElse(getEmptyModel("Unknown model"));
    }

    @Override
    public FabricBakedModel getToolModel(ForgeroTool tool) {
        return getEmptyModel(tool.getToolIdentifierString());
    }

    @Override
    public FabricBakedModel getToolPartModel(ForgeroToolPart toolPart) {
        return getEmptyModel(toolPart.getToolPartIdentifier());
    }


    private FabricBakedModel getEmptyModel(String identifier) {
        ForgeroInitializer.LOGGER.debug("Attempting to retrieve {} model from an Empty collection, return empty model", identifier);
        return new EmptyBakedModel();
    }
}
