package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.util.ForgeroResourcePOJO;

import java.util.List;

import static com.sigmundgranaas.forgero.core.property.PropertyBuilder.createPropertyListFromPOJO;
import static com.sigmundgranaas.forgero.resources.ResourceLocations.SCHEMATIC_LOCATION;

public class SchematicPOJO implements ForgeroResourcePOJO {
    public String name;
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    public PropertyPOJO properties;
    public int rarity;
    public String model;
    public int materialCount;

    public Schematic createSchematicFromPojo() {
        List<Property> propertyList = createPropertyListFromPOJO(properties);
        if (type == ForgeroToolPartTypes.HEAD) {
            return new HeadSchematic(type, name, propertyList, toolType, rarity, model, materialCount);
        } else {
            return new Schematic(type, name, propertyList, rarity, model, materialCount);
        }
    }

    @Override
    public String getResourceLocation() {
        return SCHEMATIC_LOCATION;
    }
}
