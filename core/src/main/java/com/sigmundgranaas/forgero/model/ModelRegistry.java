package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.Optional;

public class ModelRegistry {
    private TypeTree tree;
    private HashMap<String, ModelAble> modelMap;
    private TextureRegistry textureRegistry;

    public Optional<ModelAble> find(State state) {
        if (modelMap.containsKey(state.identifier())) {

        }
    }
}
