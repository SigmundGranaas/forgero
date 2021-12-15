package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Dynamic2DModelFactory {
    @NotNull
    public static DynamicModel[] createModels(ForgeroTool tool, List<ForgeroToolPartItem> bindings) {
        DynamicModel[] models = new DynamicModel[bindings.size() + 1];
        models[0] = new ToolModel2D(tool);
        for (int i = 0; i < bindings.size(); i++) {
            models[i + 1] = new ToolModel2DBinding(tool, bindings.get(i));
        }
        return models;
    }
}
