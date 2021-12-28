package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.binding.PickaxeBindingModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.binding.ShovelBindingModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.handle.HandleModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.head.PickaxeHeadModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.head.ShovelHeadModel;
import com.sigmundgranaas.forgero.item.implementation.ToolPartItemImpl;

public class Dynamic3DModelFactory {
    public static DynamicModel[] createModel(ToolPartItemImpl toolPartItem) {
        String toolPartPath = toolPartItem.getIdentifier().getPath();
        if (toolPartPath.contains("handle")) {
            DynamicModel handle = new HandleModel(toolPartItem);
            return new DynamicModel[]{handle};
        } else if (toolPartPath.contains("shovel_head")) {
            DynamicModel shovelHeadModel = new ShovelHeadModel(toolPartItem);
            return new DynamicModel[]{shovelHeadModel};
        } else if (toolPartPath.contains("pickaxe_head")) {
            DynamicModel pickaxeHeadModel = new PickaxeHeadModel(toolPartItem);
            return new DynamicModel[]{pickaxeHeadModel};
        } else {
            DynamicModel pickaxeBindingModel = new PickaxeBindingModel(toolPartItem);
            DynamicModel shovelBindingModel = new ShovelBindingModel(toolPartItem);
            return new DynamicModel[]{pickaxeBindingModel, shovelBindingModel};
        }
    }


}
