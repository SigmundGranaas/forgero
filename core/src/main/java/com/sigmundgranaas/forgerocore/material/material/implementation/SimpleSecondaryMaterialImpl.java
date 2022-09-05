package com.sigmundgranaas.forgerocore.material.material.implementation;

import com.sigmundgranaas.forgerocore.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgerocore.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgerocore.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgerocore.property.Property;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleSecondaryMaterialImpl extends AbstractForgeroMaterial implements SimpleSecondaryMaterial {

    private final List<Property> properties;

    public SimpleSecondaryMaterialImpl(MaterialPojo material) {
        super(material);

        this.properties = PropertyBuilder.createPropertyListFromPOJO(material.secondary.properties);

    }


    @Override
    public List<Property> getSecondaryProperties() {
        return Stream.of(super.getProperties(), properties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
