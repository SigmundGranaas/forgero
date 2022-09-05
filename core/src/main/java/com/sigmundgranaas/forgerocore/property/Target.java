package com.sigmundgranaas.forgerocore.property;

import com.sigmundgranaas.forgerocore.property.attribute.CompoundTarget;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The target interface represents a target for the conditions created in properties/attributes.
 * Target types are defined in TargetTypes, and the tags is only a possible match.
 * Both target type and tag needs match for the attribute to be applied when presented with a target.
 * Attributes with no condition will ignore targets.
 */
public interface Target {
    static Target EMPTY = createEmptyTarget();

    static Target createEmptyTarget() {
        return new Target() {
            @Override
            public boolean isApplicable(Set<String> tag, TargetTypes type) {
                return false;
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
        return new CompoundTarget(List.of(this, target));
    }
}
