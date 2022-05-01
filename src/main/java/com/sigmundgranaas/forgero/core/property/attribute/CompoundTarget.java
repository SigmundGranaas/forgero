package com.sigmundgranaas.forgero.core.property.attribute;

import com.sigmundgranaas.forgero.core.property.TargetTypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Target for combining lots of tags and Target types.
 * When there are lots of different targets, this class will contain them.
 */
public record CompoundTarget(List<Target> targets) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return targets.stream().flatMap(target -> target.getTypes().stream()).collect(Collectors.toSet());

    }

    @Override
    public Set<String> getTags() {
        return targets.stream().flatMap(target -> target.getTags().stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean isApplicable(Set<String> tag, TargetTypes type) {
        for (Target target : targets) {
            if (target.isApplicable(tag, type)) {
                return true;
            }
        }
        return false;
    }
}
