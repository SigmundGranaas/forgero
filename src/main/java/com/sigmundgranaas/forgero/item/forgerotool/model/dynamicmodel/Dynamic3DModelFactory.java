package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.binding.PickaxeBindingModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.binding.ShovelBindingModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.handle.HandleModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.head.PickaxeHeadModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.head.ShovelHeadModel;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;

public class Dynamic3DModelFactory implements DynamicModelFactory {
    public DynamicModel[] createModels(ForgeroToolPartItem toolPartItem) {
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
