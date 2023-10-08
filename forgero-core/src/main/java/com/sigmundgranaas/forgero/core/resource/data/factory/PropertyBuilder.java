package com.sigmundgranaas.forgero.core.resource.data.factory;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

public class PropertyBuilder {
	public static List<Property> createPropertyListFromPOJO(PropertyPojo pojo) {
		List<Property> properties = new ArrayList<>();
		if (pojo == null) {
			return properties;
		}

		if (pojo.getAttributes() != null) {
			properties.addAll(pojo.getAttributes().stream().map(AttributeBuilder::createAttributeFromPojo).toList());
		}

		if (pojo.features != null) {
			properties.addAll(pojo.features.stream().map(PropertyDataBuilder::buildFromPojo).toList());
		}
		return properties;
	}
}
