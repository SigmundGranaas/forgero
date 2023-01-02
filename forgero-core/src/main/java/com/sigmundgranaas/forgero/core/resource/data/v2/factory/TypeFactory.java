package com.sigmundgranaas.forgero.core.resource.data.v2.factory;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.namedElement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TypeFactory {
    public static List<TypeData> convert(List<DataResource> resources) {
        return new TypeFactory().convertJsonToData(resources);
    }


    public List<TypeData> convertJsonToData(List<DataResource> resources) {
        var typeResources = resources.stream()
                .filter(resource -> resource.resourceType() == ResourceType.TYPE_DEFINITION)
                .toList();
        return typeResources.stream()
                .map(this::handleTypeResource)
                .flatMap(List::stream)
                .toList();
    }

    public List<TypeData> handleTypeResource(DataResource type) {
        String name = type.name();
        TypeData data;
        if (name.equals(Identifiers.EMPTY_IDENTIFIER)) {
            return Collections.emptyList();
        }
        if (type.parent().equals(Identifiers.EMPTY_IDENTIFIER)) {
            data = new TypeData(name, Optional.empty(), Collections.emptyList());
        } else {
            data = new TypeData(name, Optional.of(type.parent()), Collections.emptyList());
        }
        if (type.children().size() > 0) {
            return ImmutableList.<TypeData>builder()
                    .add(data)
                    .addAll(createChildrenResources(type.children(), data.name()))
                    .build();
        }
        return List.of(data);
    }

    public List<TypeData> createChildrenResources(List<namedElement> children, String parent) {
        return children.stream().map(child -> new TypeData(child.name, Optional.of(parent), Collections.emptyList())).toList();
    }
}
