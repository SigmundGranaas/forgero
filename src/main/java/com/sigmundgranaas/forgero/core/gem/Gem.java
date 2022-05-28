package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.ForgeroResource;
import com.sigmundgranaas.forgero.core.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Gem extends ForgeroResource, PropertyContainer {
    @Override
    default ForgeroResourceType getResourceType() {
        return ForgeroResourceType.GEM;
    }

    int getLevel();

    Optional<Gem> upgradeGem(Gem newGem);

    Gem createGem(int level);

    default void createToolPartDescription(GemDescriptionWriter writer) {
        writer.createGemDescription(this);
    }

    default List<Property> getProperties() {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    default List<Property> getProperties(Target target) {
        return getRootProperties();
    }

    default Set<ForgeroToolPartTypes> getPlacement() {
        return Collections.emptySet();
    }
}
