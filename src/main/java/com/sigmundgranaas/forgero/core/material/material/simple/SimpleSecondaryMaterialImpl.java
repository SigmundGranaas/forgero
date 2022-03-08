package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleSecondaryMaterialImpl extends AbstractForgeroMaterial implements SimpleSecondaryMaterial {
    private final int miningLevel;
    private final float miningSpeedAddition;
    private final float attackSpeedAddition;
    private final float attackDamageAddition;
    private final List<Property> properties;

    public SimpleSecondaryMaterialImpl(SimpleMaterialPOJO material) {
        super(material);
        this.miningLevel = material.secondary.miningLevel;
        this.miningSpeedAddition = material.secondary.miningSpeedAddition;
        this.attackSpeedAddition = material.secondary.attackSpeedAddition;
        this.attackDamageAddition = material.secondary.attackDamageAddition;
        this.properties = material.secondary.properties.attributes
                .stream()
                .map(AttributeBuilder::createAttributeFromPojo)
                .collect(Collectors.toList());
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public float getMiningSpeedAddition() {
        return miningSpeedAddition;
    }

    @Override
    public float getAttackDamageAddition() {
        return attackDamageAddition;
    }

    @Override
    public float getAttackSpeedAddition() {
        return attackSpeedAddition;
    }

    @Override
    public List<Property> getSecondaryProperties() {
        return Stream.of(super.getProperties(), properties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
