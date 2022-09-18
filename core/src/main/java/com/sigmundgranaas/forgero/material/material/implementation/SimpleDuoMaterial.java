package com.sigmundgranaas.forgero.material.material.implementation;

import com.sigmundgranaas.forgero.resource.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgero.property.Property;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleDuoMaterial extends AbstractForgeroMaterial implements SimplePrimaryMaterial, SimpleSecondaryMaterial {
    private final List<Property> primaryProperties;
    private final List<Property> secondaryProperties;


    public SimpleDuoMaterial(MaterialPojo material) {
        super(material);
        primaryProperties = PropertyBuilder.createPropertyListFromPOJO(material.primary.properties);
        secondaryProperties = PropertyBuilder.createPropertyListFromPOJO(material.secondary.properties);
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
