package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;

public record CompoundTarget(Set<TargetTypes> types, Set<String> tags) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return types;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }
}
