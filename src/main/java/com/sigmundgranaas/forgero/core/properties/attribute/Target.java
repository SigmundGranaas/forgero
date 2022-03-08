package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Target {

    default boolean isApplicable(String tag, TargetTypes type) {
        return getTypes().contains(type) && getTags().contains(tag);
    }

    Set<TargetTypes> getTypes();

    Set<String> getTags();

    default Target combineTarget(Target target) {
        return new CompoundTarget(Stream.concat(target.getTypes().stream(), getTypes().stream())
                .collect(Collectors.toSet()),
                Stream.concat(target.getTags().stream(), getTags().stream())
                        .collect(Collectors.toSet()));
    }
}
