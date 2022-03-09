package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleDuoMaterial extends AbstractForgeroMaterial implements SimplePrimaryMaterial, SimpleSecondaryMaterial {
    private final List<Property> primaryProperties;
    private final List<Property> secondaryProperties;


    public SimpleDuoMaterial(SimpleMaterialPOJO material) {
        super(material);
        primaryProperties = material.primary.properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
        secondaryProperties = material.secondary.properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
    }

    @Override
    public List<Property> getPrimaryProperties() {
        return Stream.of(super.getProperties(), primaryProperties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> getSecondaryProperties() {
        return Stream.of(super.getProperties(), secondaryProperties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
