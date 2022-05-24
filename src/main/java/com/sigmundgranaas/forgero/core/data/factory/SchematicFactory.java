package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.pojo.SchematicPOJO;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.property.PropertyBuilder.createPropertyListFromPOJO;

public class SchematicFactory extends DataResourceFactory<SchematicPOJO, Schematic> {
    public SchematicFactory(List<SchematicPOJO> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    protected Optional<Schematic> createResource(SchematicPOJO pojo) {
        List<Property> propertyList = createPropertyListFromPOJO(pojo.properties);
        if (pojo.type == ForgeroToolPartTypes.HEAD) {
            return Optional.of(new HeadSchematic(pojo.type, pojo.name, propertyList, pojo.toolType, pojo.model, pojo.materialCount));
        } else {
            return Optional.of(new Schematic(pojo.type, pojo.name, propertyList, pojo.model, pojo.materialCount));
        }
    }

    @Override
    protected SchematicPOJO mergePojos(SchematicPOJO parent, SchematicPOJO child, SchematicPOJO basePojo) {
        basePojo.materialCount = replaceAttributesDefault(child.materialCount, parent.materialCount, 1);
        basePojo.model = replaceAttributesDefault(child.model, parent.model, null);
        basePojo.toolType = replaceAttributesDefault(child.toolType, parent.toolType, null);
        basePojo.type = replaceAttributesDefault(child.type, parent.type, null);
        basePojo.resourceType = ResourceType.SCHEMATIC;
        return basePojo;
    }

    @Override
    protected SchematicPOJO createDefaultPojo() {
        return new SchematicPOJO();
    }
}
