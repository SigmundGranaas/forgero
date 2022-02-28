package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;

import java.util.Collection;
import java.util.List;
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

    public List<Property> getProperties() {
        return Stream.of(primaryMaterial.getProperties(),
                        secondaryMaterial.getProperties(),
                        gem.getProperties())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
