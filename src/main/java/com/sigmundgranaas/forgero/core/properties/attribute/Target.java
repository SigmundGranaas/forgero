package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The target interface represents a target for the conditions created in properties/attributes.
 * Target types are defined in TargetTypes, and the tags is only a possible match.
 * Both target type and tag needs match for the attribute to be applied when presented with a target.
 * Attributes with no condition will ignore targets.
 */
public interface Target {
    static Target createEmptyTarget() {
        return new Target() {
            @Override
            public boolean isApplicable(Set<String> tag, TargetTypes type) {
                return true;
            }

            @Override
            public Set<TargetTypes> getTypes() {
                return Collections.emptySet();
            }

            @Override
            public Set<String> getTags() {
                return Collections.emptySet();
            }
        };
    }

    default boolean isApplicable(Set<String> tag, TargetTypes type) {
        return getTypes().contains(type) && getTags().stream().anyMatch(tag::contains);
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
