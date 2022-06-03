package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.texture.TextureModelContainerImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.data.factory.PropertyBuilder.createPropertyListFromPOJO;

public class SchematicFactory extends DataResourceFactory<SchematicPojo, Schematic> {
    public SchematicFactory(Collection<SchematicPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    @NotNull
    public Optional<Schematic> createResource(SchematicPojo pojo) {
        List<Property> propertyList = createPropertyListFromPOJO(pojo.properties);
        var modelContainer = TextureModelContainerImpl.createContainer(pojo.models);
        if (modelContainer.isPresent()) {
            if (pojo.type == ForgeroToolPartTypes.HEAD) {
                return Optional.of(new HeadSchematic(pojo.type, pojo.name, propertyList, pojo.toolType, modelContainer.get(), pojo.materialCount, pojo.unique));
            } else {
                return Optional.of(new Schematic(pojo.type, pojo.name, propertyList, modelContainer.get(), pojo.materialCount, pojo.unique));
            }
        }
        return Optional.empty();
    }

    @Override
    protected SchematicPojo mergePojos(SchematicPojo parent, SchematicPojo child, SchematicPojo basePojo) {
        basePojo.materialCount = replaceAttributesDefault(child.materialCount, parent.materialCount, 1);
        basePojo.models = replaceAttributesDefault(child.models, parent.models, null);
        basePojo.unique = replaceAttributesDefault(child.unique, parent.unique, false);
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
