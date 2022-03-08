package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.TargetTypes;
import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.properties.attribute.TargetTagSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ToolPartState {
    final PrimaryMaterial primaryMaterial;
    final SecondaryMaterial secondaryMaterial;
    final Gem gem;

    public ToolPartState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
        this.gem = gem;
    }


    public PrimaryMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }

    public SecondaryMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }

    public Gem getGem() {
        return gem;
    }

    public abstract ForgeroToolPartTypes getToolPartType();

    public List<Property> getProperties() {
        return Stream.of(primaryMaterial.getPrimaryProperties(),
                        secondaryMaterial.getSecondaryProperties(),
                        gem.getProperties())
                .flatMap(Collection::stream)
                .filter(property -> property.applyCondition(getToolPartConditionTarget()))
                .collect(Collectors.toList());

    }

    private Target getToolPartConditionTarget() {
        return new Target() {
            @Override
            public Set<TargetTypes> getTypes() {
                return Set.of(TargetTypes.TOOL_PART_TYPE);
            }

            @Override
            public TargetTagSet getTag() {
                return new TargetTagSet(Set.of(getToolPartType().toString()));
            }
        };
    }
}
