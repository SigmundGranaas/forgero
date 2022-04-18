package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;
import java.util.stream.Collectors;

public class patternPOJO {
    public String name;
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    public PropertyPOJO properties;
    public int rarity;
    public String model;
    public int materialCount;

    public Pattern createPatternFromPojo() {
        List<Property> propertyList = properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
        if (type == ForgeroToolPartTypes.HEAD) {
            return new HeadPattern(type, name, propertyList, toolType, rarity, model, materialCount);
        } else {
            return new Pattern(type, name, propertyList, rarity, model, materialCount);
        }
    }
}
