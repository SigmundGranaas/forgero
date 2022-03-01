package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;

public class EmptyTarget implements Target {
    public static Target createEmptyTarget() {
        return new EmptyTarget();
    }

    @Override
    public Set<TargetTypes> getTypes() {
        return Set.of(TargetTypes.TOOL_PART_TYPE);
    }

    @Override
    public TargetTagSet getTag() {
        return new TargetTagSet(Set.of("HEAD", "HANDLE", "BINDING"));
    }
}
