package com.sigmundgranaas.forgero.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.Optional;

public class ModelRegistry {
    private final TypeTree tree;
    private final HashMap<String, ModelMatcher> modelMap;

    public ModelRegistry(TypeTree tree) {
        this.tree = tree;
        this.modelMap = new HashMap<>();
    }

    public ModelRegistry register(DataResource data) {
        var converter = new ModelConverter(tree, modelMap);
        converter.register(data);
        return this;
    }

    public Optional<ModelTemplate> find(State state) {
        if (modelMap.containsKey(state.identifier())) {
            return modelMap.get(state.identifier()).find(state, (String id) -> Optional.ofNullable(modelMap.get(id)));
        } else {
            var modelEntries = tree.find(state.type().typeName()).map(node -> node.getResources(ModelMatcher.class)).orElse(ImmutableList.<ModelMatcher>builder().build());
            return modelEntries.stream().map(entry -> entry.find(state, (String id) -> Optional.ofNullable(modelMap.get(id)))).filter(Optional::isPresent).flatMap(Optional::stream).findAny();
        }
    }
}
