package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Dynamic2DModelFactory {
    @NotNull
    public static DynamicModel[] createModels(ForgeroToolItem tool, List<ToolPartItem> bindings) {
        DynamicModel[] models = new DynamicModel[bindings.size() + 1];
        models[0] = new ToolModel2D(tool);
        for (int i = 0; i < bindings.size(); i++) {
            models[i + 1] = new ToolModel2DBinding(tool, bindings.get(i));
        }
        return models;
    }
}
