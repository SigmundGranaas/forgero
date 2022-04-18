package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Gem {
    String getIdentifier();

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

    default Set<ForgeroToolPartTypes> getPlacement() {
        return Collections.emptySet();
    }
}
