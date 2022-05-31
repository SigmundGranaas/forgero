package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.data.factory.PropertyBuilder.createPropertyListFromPOJO;

public class SchematicFactory extends DataResourceFactory<SchematicPojo, Schematic> {
    public SchematicFactory(List<SchematicPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    public Optional<Schematic> createResource(SchematicPojo pojo) {
        List<Property> propertyList = createPropertyListFromPOJO(pojo.properties);
        if (pojo.type == ForgeroToolPartTypes.HEAD) {
            return Optional.of(new HeadSchematic(pojo.type, pojo.name, propertyList, pojo.toolType, pojo.model, pojo.materialCount));
        } else {
            return Optional.of(new Schematic(pojo.type, pojo.name, propertyList, pojo.model, pojo.materialCount));
        }
    }

    @Override
    protected SchematicPojo mergePojos(SchematicPojo parent, SchematicPojo child, SchematicPojo basePojo) {
        basePojo.materialCount = replaceAttributesDefault(child.materialCount, parent.materialCount, 1);
        basePojo.model = replaceAttributesDefault(child.model, parent.model, null);
        basePojo.toolType = replaceAttributesDefault(child.toolType, parent.toolType, null);
        basePojo.type = replaceAttributesDefault(child.type, parent.type, null);
        basePojo.resourceType = ResourceType.SCHEMATIC;
        return basePojo;
    }

    @Override
    protected SchematicPojo createDefaultPojo() {
        return new SchematicPojo();
    }
}
