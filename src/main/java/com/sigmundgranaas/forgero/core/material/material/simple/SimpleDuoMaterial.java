package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleDuoMaterial extends AbstractForgeroMaterial implements SimplePrimaryMaterial, SimpleSecondaryMaterial {
    private final int miningLevel;
    private final float miningSpeed;
    private final int attackDamage;
    private final float attackSpeed;

    private final float miningSpeedAddition;
    private final float attackDamageAddition;
    private final float attackSpeedAddition;
    private final List<Property> primaryProperties;
    private final List<Property> secondaryProperties;


    public SimpleDuoMaterial(SimpleMaterialPOJO material) {
        super(material);
        miningLevel = material.primary.miningLevel;
        miningSpeed = material.primary.miningSpeed;
        attackDamage = material.primary.attackDamage;
        attackSpeed = material.primary.attackSpeed;
        miningSpeedAddition = material.secondary.miningSpeedAddition;
        attackDamageAddition = material.secondary.attackDamageAddition;
        attackSpeedAddition = material.secondary.attackSpeedAddition;
        primaryProperties = material.primary.properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
        secondaryProperties = material.secondary.properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList());
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
    public float getMiningSpeed() {
        return miningSpeed;
    }

    @Override
    public int getAttackDamage() {
        return attackDamage;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
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
