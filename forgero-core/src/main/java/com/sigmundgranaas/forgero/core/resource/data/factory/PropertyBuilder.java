package com.sigmundgranaas.forgero.core.resource.data.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureRegistry;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.util.Identifiers;

public class PropertyBuilder {
	public static List<Property> createPropertyListFromPOJO(PropertyPojo pojo) {
		List<Property> properties = new ArrayList<>();

		if (pojo == null) {
			return properties;
		}

		if (pojo.getAttributes() != null) {
			List<Attribute> rootAttributes = pojo.getAttributes().stream()
					.map(AttributeBuilder::createAttributeFromPojo)
					.toList();
			properties.addAll(filterAttributeOverrides(rootAttributes));
		}

		if (pojo.features != null) {
			properties.addAll(pojo.features.stream()
					.map(FeatureRegistry::of)
					.flatMap(Optional::stream)
					.toList());
		}
		return properties;
	}

	public static List<Attribute> filterAttributeOverrides(List<Attribute> rootAttributes) {
		Map<String, Attribute> idMap = rootAttributes
				.stream()
				.filter(attribute -> !attribute.getId().equals(Identifiers.EMPTY_IDENTIFIER))
				.collect(Collectors.toMap(Attribute::getId, attribute -> attribute, (existing, replacement) -> existing.getPriority() > replacement.getPriority() ? existing : replacement));

		var nonIdAttributes = rootAttributes
				.stream()
				.filter(attribute -> attribute.getId().equals(Identifiers.EMPTY_IDENTIFIER))
				.toList();

		return Stream.of(idMap.values(), nonIdAttributes)
				.flatMap(Collection::stream)
				.sorted(Attribute::compareTo)
				.toList();
	}
}

