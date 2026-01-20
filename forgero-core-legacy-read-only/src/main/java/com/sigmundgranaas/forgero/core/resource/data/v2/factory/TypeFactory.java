package com.sigmundgranaas.forgero.core.resource.data.v2.factory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResourceSerializer;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.namedElement;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class TypeFactory {
	public static List<TypeData> convert(List<DataResource> resources) {
		return new TypeFactory().convertJsonToData(resources);
	}


	public List<TypeData> convertJsonToData(List<DataResource> resources) {
		return resources.stream()
				.filter(resource -> resource.resourceType() == ResourceType.TYPE_DEFINITION)
				.map(this::handleTypeResource)
				.flatMap(List::stream)
				.toList();
	}

	public List<TypeData> handleTypeResource(DataResource type) {
		String name = type.name();
		TypeData data;
		if (name.equals(EMPTY_IDENTIFIER)) {
			DataResourceSerializer serializer = new DataResourceSerializer();
			Forgero.LOGGER.error("Empty identifier for name: {}", serializer.toJson(type));
			throw new IllegalArgumentException("Empty name field is not allowed in type definitions.");
		}
		if (type.parent().equals(EMPTY_IDENTIFIER)) {
			data = new TypeData(name, Optional.empty(), Collections.emptyList());
		} else {
			data = new TypeData(name, Optional.of(type.parent()), Collections.emptyList());
		}
		if (!type.children().isEmpty()) {
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
