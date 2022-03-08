package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.properties.Property;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface Gem {
    String getIdentifier();

    GemTypes getType();

    int getLevel();

    Optional<Gem> upgradeGem(Gem newGem);

    Gem createGem(int level);

    String getName();

    default void createToolPartDescription(GemDescriptionWriter writer) {
        writer.createGemDescription(this);
    }

    default List<Property> getProperties() {
        return Collections.emptyList();
    }
}
