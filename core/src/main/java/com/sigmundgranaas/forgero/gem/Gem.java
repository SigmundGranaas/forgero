package com.sigmundgranaas.forgero.gem;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.resource.ForgeroResource;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Gem extends ForgeroResource<GemPojo>, PropertyContainer {
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

    default @NotNull List<Property> getProperties() {
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
