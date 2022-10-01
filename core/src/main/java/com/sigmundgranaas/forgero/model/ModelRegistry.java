package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.Optional;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class ModelRegistry {
    private TypeTree tree;
    private HashMap<String, ModelAssembly> modelMap;
    private TextureRegistry textureRegistry;

    public void register(ModelAssembly data) {
        if (!data.getName().equals(EMPTY_IDENTIFIER)) {
            modelMap.put(data.getName(), data);
        }
        if (data.getTarget().size() == 1) {
            if (data.getTarget().get(0).contains("id:")) {
                modelMap.put(data.getTarget().get(0), data);
            } else if (data.getTarget().get(0).toUpperCase().equals(data.getTarget().get(0))) {
                tree.find(data.getTarget().get(0)).ifPresent(node -> node.addResource(data, ModelData.class));
            }
        }
    }

    public Optional<Model> find(State state) {
        if (modelMap.containsKey(state.identifier())) {

        }
    }
}
