package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.properties.Attribute;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeroGem implements Gem {
    private final int gemLevel;
    private final String identifier;
    private final List<Property> propertyList;
    private final List<ForgeroToolPartTypes> placement;

    public ForgeroGem(int gemLevel, String identifier, List<Property> propertyList, List<ForgeroToolPartTypes> placement) {
        this.propertyList = propertyList;
        this.placement = placement;
        if (gemLevel < 1) {
            throw new IllegalArgumentException("Gem level cannot be under 1");
        }
        this.gemLevel = gemLevel;
        this.identifier = identifier;
    }

    @Override
    public Set<ForgeroToolPartTypes> getPlacement() {
        return Set.copyOf(placement);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getLevel() {
        return gemLevel;
    }

    @Override
    public Optional<Gem> upgradeGem(Gem newGem) {
        if (equals(newGem) && newGem.getIdentifier().equals(this.getIdentifier())) {
            return Optional.of(this.createGem(getLevel() + 1));
        }
        return Optional.empty();
    }

    @Override
    public Gem createGem(int level) {
        return new ForgeroGem(level, getIdentifier(), propertyList, placement);
    }

    public boolean equals(Gem newGem) {
        return identifier.equals(newGem.getIdentifier()) && newGem.getLevel() == getLevel();
    }

    @Override
    public String getName() {
        return getIdentifier().split("_")[0];
    }

    @Override
    public List<Property> getProperties() {
        return propertyList.stream()
                .filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast)
                .map(attribute -> {
                    AttributeBuilder builder = AttributeBuilder.createAttributeBuilderFromAttribute(attribute);
                    builder.applyLevel(gemLevel);
                    return builder.build();
                }).collect(Collectors.toList());
    }
}
