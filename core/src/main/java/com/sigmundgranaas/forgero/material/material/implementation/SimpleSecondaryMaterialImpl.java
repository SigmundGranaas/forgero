package com.sigmundgranaas.forgero.material.material.implementation;

import com.sigmundgranaas.forgero.resource.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgero.property.Property;

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
