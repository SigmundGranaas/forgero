package com.sigmundgranaas.forgero.core.data.factory;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class GemFactory extends DataResourceFactory<GemPojo, Gem> {
    public GemFactory(List<GemPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    public Optional<Gem> createResource(GemPojo pojo) {
        String name = pojo.name.toLowerCase(Locale.ROOT);
        var properties = PropertyBuilder.createPropertyListFromPOJO(pojo.properties);
        return Optional.of(new ForgeroGem(1, name + "_gem", properties, pojo.placement));
    }

    @Override
    public ImmutableList<Gem> createResources() {
        return null;
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
