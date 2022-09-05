package com.sigmundgranaas.forgerocore.material.material.implementation;

import com.sigmundgranaas.forgerocore.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgerocore.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgerocore.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgerocore.property.Property;

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
