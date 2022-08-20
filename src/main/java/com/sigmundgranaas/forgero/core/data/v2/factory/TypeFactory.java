package com.sigmundgranaas.forgero.core.data.v2.factory;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonChild;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.core.data.v2.json.JsonResourceType.TYPE_DEFINITION;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class TypeFactory {

    public List<TypeData> convertJsonToData(List<JsonResource> resources) {
        var typeResources = resources.stream().filter(resource -> resource.resourceType == TYPE_DEFINITION).map(JsonResource::applyDefaults).toList();
        return typeResources.stream().map(this::handleTypeResource).flatMap(List::stream).toList();
    }

    public List<TypeData> handleTypeResource(JsonResource type) {
        String name = type.name;
        TypeData data;
        if (name.equals(EMPTY_IDENTIFIER)) {
            return Collections.emptyList();
        }
        if (type.parent.equals(EMPTY_IDENTIFIER)) {
            data = new TypeData(name, Optional.empty());
        } else {
            data = new TypeData(name, Optional.of(type.parent));
        }
        if (type.children.size() > 0) {
            return ImmutableList.<TypeData>builder()
                    .add(data)
                    .addAll(createChildrenResources(type.children, data.name()))
                    .build();
        }
        return List.of(data);
    }

    public List<TypeData> createChildrenResources(List<JsonChild> children, String parent) {
        return children.stream().map(child -> new TypeData(child.name, Optional.of(parent))).toList();
    }
}
