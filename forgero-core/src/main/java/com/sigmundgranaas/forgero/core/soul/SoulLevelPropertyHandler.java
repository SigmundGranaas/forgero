package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.property.Property;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SoulLevelPropertyHandler {
    private final Map<String, PropertyLevelProvider> levelPropertyProvider;

    public SoulLevelPropertyHandler(Map<String, PropertyLevelProvider> levelPropertyProvider) {
        this.levelPropertyProvider = levelPropertyProvider;
    }

    public List<Property> apply(int level, String type) {
        return Optional.ofNullable(levelPropertyProvider.get(type)).map(provider -> provider.apply(level)).orElse(Collections.emptyList());
    }
}
