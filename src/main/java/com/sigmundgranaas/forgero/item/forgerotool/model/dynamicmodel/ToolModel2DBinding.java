package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroMiningTool;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;

public class ToolModel2DBinding extends ToolModel2D {
    private final ForgeroToolPartItem binding;


    public ToolModel2DBinding(ForgeroTool tool, ForgeroToolPartItem binding) {
        super(tool);
        this.binding = binding;
    }

    @Override
    protected JsonElement getTextures() {
        JsonObject textures = super.getTextures().getAsJsonObject();
        if (tool instanceof ForgeroMiningTool) {
            textures.addProperty("layer2", super.getTextureBase() + binding.getMaterial().toString().toLowerCase() + "_" + ToolPartModelTypeToFilename(getBindingType()));
        }
        return textures;
    }

    @Override
    public String itemPartModelIdentifier() {
        String bindingType = ((ForgeroMiningTool) tool).getToolTypeLowerCaseString();
        return tool.getIdentifier().getPath() + "_" + binding.getToolPartTypeAndMaterialLowerCase() + "_toolpart" + "_" + bindingType;
    }
}
