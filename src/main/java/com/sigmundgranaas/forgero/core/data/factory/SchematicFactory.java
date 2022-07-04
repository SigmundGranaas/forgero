package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelContainerPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.schematic.SlotContainer;
import com.sigmundgranaas.forgero.core.texture.TextureModelContainerImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
        var slotContainer = SlotContainer.createContainer(pojo.slots);
        if (modelContainer.isPresent()) {
            if (pojo.type == ForgeroToolPartTypes.HEAD) {
                return Optional.of(new HeadSchematic(pojo.type, pojo.name, propertyList, pojo.toolType, modelContainer.get(), pojo.materialCount, pojo.unique, slotContainer));
            } else {
                return Optional.of(new Schematic(pojo.type, pojo.name, propertyList, modelContainer.get(), pojo.materialCount, pojo.unique, slotContainer));
            }
        }
        return Optional.empty();
    }

    @Override
    protected SchematicPojo mergePojos(SchematicPojo parent, SchematicPojo child, SchematicPojo basePojo) {
        basePojo.materialCount = replaceAttributesDefault(child.materialCount, parent.materialCount, 1);
        basePojo.models = mergeModels(child.models, parent.models);
        basePojo.slots = mergeSlots(child.slots, parent.slots);
        basePojo.unique = replaceAttributesDefault(child.unique, parent.unique, false);
        basePojo.toolType = replaceAttributesDefault(child.toolType, parent.toolType, null);
        basePojo.type = replaceAttributesDefault(child.type, parent.type, null);
        basePojo.resourceType = ResourceType.SCHEMATIC;
        return basePojo;
    }

    private List<ModelContainerPojo> mergeModels(Collection<ModelContainerPojo> child, Collection<ModelContainerPojo> parent) {
        var parentModel = parent.stream().filter(modelContainerPojo -> modelContainerPojo.model != null).collect(Collectors.toMap(container -> container.id, container -> container));
        child.forEach(model -> {
            if (model != null) parentModel.put(model.id, model);
        });
        return parentModel.values().stream().toList();
    }

    private List<SchematicPojo.SlotDataContainer> mergeSlots(Collection<SchematicPojo.SlotDataContainer> child, Collection<SchematicPojo.SlotDataContainer> parent) {
        var parentSlots = parent.stream().filter(Objects::nonNull).toList();
        child.forEach(container -> {
           if(parentSlots.stream().noneMatch( matchingContainer -> matchingContainer.index == container.index && container.type.equals(matchingContainer.type))){
              parentSlots.add(container);
           }
        });
        return parentSlots;
    }

    @Override
    protected SchematicPojo createDefaultPojo() {
        return new SchematicPojo();
    }
}
