package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.property.active.ActivePropertyBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class PropertyBuilder {
    public static List<Property> createPropertyListFromPOJO(PropertyPOJO pojo) {
        List<Property> properties = pojo.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
        properties.addAll(pojo.active.stream().map(ActivePropertyBuilder::createAttributeFromPojo).toList());
        return properties;
    }
}
