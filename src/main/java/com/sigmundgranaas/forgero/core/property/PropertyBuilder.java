package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.data.pojo.PropertyPOJO;
import com.sigmundgranaas.forgero.core.property.active.ActivePropertyBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.passive.PassivePropertyBuilder;

import java.util.ArrayList;
import java.util.List;

public class PropertyBuilder {
    public static List<Property> createPropertyListFromPOJO(PropertyPOJO pojo) {
        List<Property> properties = new ArrayList<>();
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
