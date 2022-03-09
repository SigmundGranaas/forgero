package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;

/**
 * Target for combining lots of tags and Target types.
 * When there are lots of different targets, this class will contain them.
 */
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
