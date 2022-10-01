package com.sigmundgranaas.forgero.model;

import java.util.List;

public class BasedCompositeModel extends CompositeModel {
    private final ModelAble base;

    public BasedCompositeModel(List<ModelAble> models, ModelAble base) {
        super(models);
        this.base = base;
    }

    public ModelAble getBase() {
        return base;
    }
}
