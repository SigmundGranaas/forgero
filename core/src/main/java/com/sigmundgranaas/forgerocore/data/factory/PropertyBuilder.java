package com.sigmundgranaas.forgerocore.data.factory;

import com.sigmundgranaas.forgerocore.data.v1.pojo.PropertyPojo;
import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.active.ActivePropertyBuilder;
import com.sigmundgranaas.forgerocore.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgerocore.property.passive.PassivePropertyBuilder;

import java.util.ArrayList;
import java.util.List;

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
            properties.addAll(pojo.active.stream().map(ActivePropertyBuilder::createAttributeFromPojo).toList());

        }
        if (pojo.passiveProperties != null && !pojo.passiveProperties.isEmpty()) {
            properties.addAll(pojo.passiveProperties.stream().map(PassivePropertyBuilder::createPassivePropertyFromPojo).toList());
        }
        return properties;
    }
}
