package com.sigmundgranaas.forgero.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.ResourceListener;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.*;

import static com.sigmundgranaas.forgero.resource.data.v2.data.ResourceType.MODEL;

public class ModelRegistry {
    private final HashMap<String, ModelMatcher> modelMap;
    private final Set<String> textures;
    private TypeTree tree;

    public ModelRegistry(TypeTree tree) {
        this.tree = tree;
        this.modelMap = new HashMap<>();
        this.textures = new HashSet<>();
    }

    public ModelRegistry() {
        this.tree = new TypeTree();
        this.modelMap = new HashMap<>();
        this.textures = new HashSet<>();
    }

    public ResourceListener<List<DataResource>> modelListener() {
        return (resources, tree, idMapper) -> {
            this.tree = tree;
            resources.stream().filter(resource -> resource.resourceType() == MODEL).forEach(this::register);
        };
    }

    public ResourceListener<List<DataResource>> paletteListener() {
        return (resources, tree, idMapper) -> {
            resources.stream().filter(res -> res.palette().isPresent()).forEach(res -> tree.find(res.type()).ifPresent(node -> node.addResource(res.palette().get(), PaletteData.class)));
        };
    }

    public void setTree(TypeTree tree) {
        this.tree = tree;
    }

    public ModelRegistry register(DataResource data) {
        var converter = new ModelConverter(tree, modelMap, textures);
        converter.register(data);
        return this;
    }

    public Optional<ModelTemplate> find(State state) {
        if (modelMap.containsKey(state.identifier())) {
            return modelMap.get(state.identifier()).match(state, this::provider);
        } else {
            var modelEntries = tree.find(state.type().typeName()).map(node -> node.getResources(ModelMatcher.class)).orElse(ImmutableList.<ModelMatcher>builder().build());
            return modelEntries.stream().map(entry -> entry.match(state, this::provider)).filter(Optional::isPresent).flatMap(Optional::stream).findAny();
        }
    }

    public Optional<ModelMatcher> provider(Identifiable id) {
        if (modelMap.containsKey(id.identifier())) {
            return Optional.ofNullable(modelMap.get(id.identifier()));
        } else if (modelMap.containsKey(id.name())) {
            return Optional.ofNullable(modelMap.get(id.name()));
        } else if (id instanceof State state) {
            return Optional.of(MultipleModelMatcher.of(tree.find(state.type().typeName()).map(node -> node.getResources(ModelMatcher.class)).orElse(ImmutableList.<ModelMatcher>builder().build())));
        }
        return Optional.empty();
    }

    public Set<String> getTextures() {
        return textures;
    }
}
