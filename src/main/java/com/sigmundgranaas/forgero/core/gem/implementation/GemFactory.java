package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;

import java.util.Locale;
import java.util.stream.Collectors;

public class GemFactory {
    public Gem createGem(GemPOJO gemPOJO) {
        String name = gemPOJO.name.toLowerCase(Locale.ROOT);
        return new ForgeroGem(1, name + "_gem", gemPOJO.properties.attributes.stream().map(AttributeBuilder::createAttributeFromPojo).collect(Collectors.toList()), gemPOJO.placement);

    }
}
