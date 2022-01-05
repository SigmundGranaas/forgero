package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ToolPartItem;

public class ToolModel2DBinding extends ToolModel2D {
    private final ToolPartItem binding;


    public ToolModel2DBinding(ForgeroToolItem tool, ToolPartItem binding) {
        super(tool);
        this.binding = binding;
    }

    @Override
    protected JsonElement getTextures() {
        JsonObject textures = super.getTextures().getAsJsonObject();
        if (tool instanceof ForgeroToolItem) {
            textures.addProperty("layer3", super.getTextureBase() + binding.getPrimaryMaterial().getName() + "_" + ToolPartModelTypeToFilename(getBindingType()));
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
