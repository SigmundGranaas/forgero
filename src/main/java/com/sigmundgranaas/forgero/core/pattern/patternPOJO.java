package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

import static com.sigmundgranaas.forgero.core.property.PropertyBuilder.createPropertyListFromPOJO;

public class patternPOJO {
    public String name;
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    public PropertyPOJO properties;
    public int rarity;
    public String model;
    public int materialCount;

    public Pattern createPatternFromPojo() {
        List<Property> propertyList = createPropertyListFromPOJO(properties);
        if (type == ForgeroToolPartTypes.HEAD) {
            return new HeadPattern(type, name, propertyList, toolType, rarity, model, materialCount);
        } else {
            return new Pattern(type, name, propertyList, rarity, model, materialCount);
        }
    }
}
