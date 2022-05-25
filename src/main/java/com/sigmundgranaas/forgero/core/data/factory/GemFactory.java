package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.GemPOJO;
import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class GemFactory extends DataResourceFactory<GemPOJO, Gem> {
    public GemFactory(List<GemPOJO> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    protected Optional<Gem> createResource(GemPOJO pojo) {
        String name = pojo.name.toLowerCase(Locale.ROOT);
        var properties = PropertyBuilder.createPropertyListFromPOJO(pojo.properties);
        return Optional.of(new ForgeroGem(1, name + "_gem", properties, pojo.placement));
    }

    @Override
    protected GemPOJO mergePojos(GemPOJO parent, GemPOJO child, GemPOJO basePojo) {
        basePojo.placement = replaceAttributesDefault(child.placement, parent.placement, null);

        basePojo.resourceType = ResourceType.GEM;
        return basePojo;
    }

    @Override
    protected GemPOJO createDefaultPojo() {
        return new GemPOJO();
    }
}
