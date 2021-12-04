package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;

public class Dynamic2DModelFactory {
    public static DynamicModel[] createModels(ForgeroTool tool) {
        return new DynamicModel[]{new ToolModel2D(tool)};
    }
}
