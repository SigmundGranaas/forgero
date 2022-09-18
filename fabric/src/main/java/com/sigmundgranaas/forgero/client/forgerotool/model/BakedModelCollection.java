package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface BakedModelCollection {
    Map<String, FabricBakedModel> getBakedToolPartModels();

    FabricBakedModel getModel(String identifier);

    FabricBakedModel getModel(ItemStack stack);

    FabricBakedModel getToolModel(ItemStack forgeroToolStack);

    FabricBakedModel getToolPartModel(ItemStack ForgeroToolPartModel);

    FabricBakedModel getToolModel(ForgeroTool tool);

    FabricBakedModel getToolPartModel(ForgeroToolPart toolPart);

    FabricBakedModel getModel(Composite composite);

    FabricBakedModel getModel(Item item);
}
