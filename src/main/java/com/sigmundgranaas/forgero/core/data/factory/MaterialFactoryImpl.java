package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.SchemaVersion;
import com.sigmundgranaas.forgero.core.data.pojo.PropertyPOJO;
import com.sigmundgranaas.forgero.core.data.pojo.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleSecondaryMaterialImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MaterialFactoryImpl extends DataResourceFactory<SimpleMaterialPOJO, ForgeroMaterial> implements MaterialFactory {
    private static MaterialFactoryImpl INSTANCE;

    public MaterialFactoryImpl() {
    }

    public MaterialFactoryImpl(List<SimpleMaterialPOJO> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }


    public static MaterialFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFactoryImpl();
        }
        return INSTANCE;
    }

    protected SimpleMaterialPOJO mergePojos(SimpleMaterialPOJO parent, SimpleMaterialPOJO material) {
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

    @Override
    protected Optional<ForgeroMaterial> createResource(SimpleMaterialPOJO pojo) {
        if (pojo.primary != null) {
            return Optional.of(new SimpleDuoMaterial(pojo));
        } else {
            return Optional.of(new SimpleSecondaryMaterialImpl(pojo));
        }
    }
}
