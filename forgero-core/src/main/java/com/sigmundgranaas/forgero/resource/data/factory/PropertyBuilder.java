package com.sigmundgranaas.forgero.resource.data.factory;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.active.ActivePropertyBuilder;
import com.sigmundgranaas.forgero.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.property.passive.PassivePropertyBuilder;
import com.sigmundgranaas.forgero.resource.data.PropertyPojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyBuilder {
    public static List<Property> createPropertyListFromPOJO(PropertyPojo pojo) {
        List<Property> properties = new ArrayList<>();
        if (pojo == null) {
            return properties;
        }

        if (pojo.attributes != null) {
            properties.addAll(pojo.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).toList());
        }
        if (pojo.active != null) {
            properties.addAll(pojo.active.stream().map(ActivePropertyBuilder::createAttributeFromPojo).flatMap(Optional::stream).toList());

        }
        if (pojo.passiveProperties != null && !pojo.passiveProperties.isEmpty()) {
            properties.addAll(pojo.passiveProperties.stream().map(PassivePropertyBuilder::createPassivePropertyFromPojo).toList());
        }
        return properties;
    }
}
