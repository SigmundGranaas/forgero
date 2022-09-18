package com.sigmundgranaas.forgero.gem;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.property.Attribute;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

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
    public String getStringIdentifier() {
        return identifier;
    }

    @Override
    public int getLevel() {
        return gemLevel;
    }

    @Override
    public Optional<Gem> upgradeGem(Gem newGem) {
        if (equals(newGem) && newGem.getStringIdentifier().equals(this.getStringIdentifier())) {
            return Optional.of(this.createGem(getLevel() + 1));
        }
        return Optional.empty();
    }

    @Override
    public Gem createGem(int level) {
        return new ForgeroGem(level, getStringIdentifier(), propertyList, placement);
    }

    public boolean equals(Gem newGem) {
        return identifier.equals(newGem.getStringIdentifier()) && newGem.getLevel() == getLevel();
    }

    @Override
    public String getResourceName() {
        return getStringIdentifier().split(ELEMENT_SEPARATOR)[0];
    }

    @Override
    public GemPojo toDataResource() {
        return null;
    }

    @Override
    public @NotNull List<Property> getProperties() {
        var leveledAttribute = propertyList.stream().filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast)
                .map(attribute -> {
                    AttributeBuilder builder = AttributeBuilder.createAttributeBuilderFromAttribute(attribute);
                    builder.applyLevel(gemLevel);
                    return builder.build();
                }).toList();
        var otherAttributes = propertyList.stream().filter(property -> !(property instanceof Attribute)).toList();
        return Stream.of(leveledAttribute, otherAttributes).flatMap(List::stream).collect(Collectors.toList());
    }
}
