package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;

public interface DynamicModelFactory {
    public DynamicModel[] createModels(ForgeroToolPartItem toolPartItem);
}
