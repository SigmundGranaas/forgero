package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItem;

public class ToolModel2DBinding extends ToolModel2D {
    private final ForgeroToolPartItem binding;


    public ToolModel2DBinding(ForgeroToolItem tool, ForgeroToolPartItem binding) {
        super(tool);
        this.binding = binding;
    }

    @Override
    protected JsonElement getTextures() {
        JsonObject textures = super.getTextures().getAsJsonObject();
        if (tool instanceof ForgeroToolItem) {
            textures.addProperty("layer2", super.getTextureBase() + binding.getPrimaryMaterial().getName() + "_" + ToolPartModelTypeToFilename(getBindingType()));
        }
        return textures;
    }

    @Override
    public String itemPartModelIdentifier() {
        String bindingType = tool.getTool().getToolHead().getToolTypeName();
        String toolIdentifier = tool.getTool().getToolIdentifierString();
        return toolIdentifier + "_" + binding.getPart().getToolPartIdentifier() + "_toolpart" + "_" + bindingType;
    }
}
