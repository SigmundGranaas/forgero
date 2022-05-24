package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.SchemaVersion;
import com.sigmundgranaas.forgero.core.data.pojo.PropertyPOJO;
import com.sigmundgranaas.forgero.core.data.pojo.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleSecondaryMaterialImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaterialFactoryImpl implements MaterialFactory {
    private static MaterialFactoryImpl INSTANCE;
    Map<String, SimpleMaterialPOJO> materialPOJOS = new HashMap<>();
    Set<String> availableNameSpaces = new HashSet<>();

    public MaterialFactoryImpl() {
    }

    public MaterialFactoryImpl(List<SimpleMaterialPOJO> pojos, Set<String> availableNameSpaces) {
        this.materialPOJOS = pojos.stream().collect(Collectors.toMap(pojo -> pojo.name, pojo -> pojo));
        this.availableNameSpaces = availableNameSpaces;
    }


    public static MaterialFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFactoryImpl();
        }
        return INSTANCE;
    }

    public static <T> T replaceAttributesDefault(T attribute1, T attribute2, T defaultAttribute) {
        if (attribute1 == null && attribute2 == null)
            return defaultAttribute;
        else return Objects.requireNonNullElse(attribute1, attribute2);
    }

    public static <T> T attributeOrDefault(T attribute1, T defaultAttribute) {
        return Objects.requireNonNullElse(attribute1, defaultAttribute);
    }

    public static <T> List<T> mergeAttributes(List<T> attribute1, List<T> attribute2) {
        if (attribute1 == null && attribute2 == null)
            return Collections.emptyList();
        else if (attribute1 != null && attribute2 != null) {
            return Stream.of(attribute1, attribute2).flatMap(List::stream).distinct().toList();
        } else return Objects.requireNonNullElse(attribute1, attribute2);
    }

    @Override
    public Optional<ForgeroMaterial> createMaterial(SimpleMaterialPOJO material) {
        if (material.abstractResource) {
            return Optional.empty();
        }
        if (material.parent == null) {
            return createMaterialFromPojo(material);
        }
        return assemblePojoFromParent(material).flatMap(this::createMaterialFromPojo);
    }

    private Optional<SimpleMaterialPOJO> assemblePojoFromParent(SimpleMaterialPOJO material) {
        if (material.parent == null) {
            return Optional.of(material);
        }
        if (materialPOJOS.containsKey(material.parent)) {
            var parentOpt = assemblePojoFromParent(materialPOJOS.get(material.parent));
            return parentOpt
                    .map(simpleMaterialPOJO -> mergePojos(simpleMaterialPOJO, material))
                    .or(() -> Optional.of(material));
        }
        return Optional.empty();
    }

    private SimpleMaterialPOJO mergePojos(SimpleMaterialPOJO parent, SimpleMaterialPOJO material) {
        var newMaterial = new SimpleMaterialPOJO();
        //Some attributes should always be fetched from the child
        newMaterial.name = replaceAttributesDefault(material.name, parent.name, null);
        newMaterial.version = replaceAttributesDefault(material.version, parent.version, SchemaVersion.V1);
        newMaterial.parent = material.parent;
        newMaterial.resourceType = ResourceType.MATERIAL;
        newMaterial.type = replaceAttributesDefault(material.type, parent.type, null);
        newMaterial.primary = material.primary;
        newMaterial.secondary = material.secondary;
        newMaterial.palette = material.palette;

        //TODO create a proper way of handling ingredients
        newMaterial.ingredient = replaceAttributesDefault(material.ingredient, parent.ingredient, null);


        //merging dependencies
        newMaterial.dependencies = mergeAttributes(material.dependencies, parent.dependencies);

        //Merging properties
        newMaterial.properties = new PropertyPOJO();
        newMaterial.properties.active = mergeAttributes(attributeOrDefault(material.properties, new PropertyPOJO()).active, attributeOrDefault(parent.properties, new PropertyPOJO()).active);
        newMaterial.properties.passiveProperties = mergeAttributes(attributeOrDefault(material.properties, new PropertyPOJO()).passiveProperties, attributeOrDefault(parent.properties, new PropertyPOJO()).passiveProperties);
        newMaterial.properties.attributes = mergeAttributes(attributeOrDefault(material.properties, new PropertyPOJO()).attributes, attributeOrDefault(parent.properties, new PropertyPOJO()).attributes);

        return newMaterial;
    }

    private Optional<ForgeroMaterial> createMaterialFromPojo(SimpleMaterialPOJO material) {
        if (material.dependencies != null && !availableNameSpaces.containsAll(material.dependencies)) {
            return Optional.empty();
        }
        if (material.primary != null) {
            return Optional.of(new SimpleDuoMaterial(material));
        } else {
            return Optional.of(new SimpleSecondaryMaterialImpl(material));
        }
    }
}
