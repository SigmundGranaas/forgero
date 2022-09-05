package com.sigmundgranaas.forgerocore.data.factory;

import com.sigmundgranaas.forgerocore.data.v1.ResourceType;
import com.sigmundgranaas.forgerocore.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgerocore.gem.ForgeroGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

public class GemFactory extends DataResourceFactory<GemPojo, Gem> {
    public GemFactory(Collection<GemPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    public @NotNull Optional<Gem> createResource(GemPojo pojo) {
        String name = pojo.name.toLowerCase(Locale.ROOT);
        var properties = PropertyBuilder.createPropertyListFromPOJO(pojo.properties);
        return Optional.of(new ForgeroGem(1, name + ELEMENT_SEPARATOR + "gem", properties, pojo.placement));
    }

    @Override
    protected GemPojo mergePojos(GemPojo parent, GemPojo child, GemPojo basePojo) {
        basePojo.placement = replaceAttributesDefault(child.placement, parent.placement, null);

        basePojo.resourceType = ResourceType.GEM;
        return basePojo;
    }

    @Override
    protected GemPojo createDefaultPojo() {
        return new GemPojo();
    }
}
