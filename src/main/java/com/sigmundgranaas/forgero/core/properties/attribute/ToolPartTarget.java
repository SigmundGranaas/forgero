package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;

public record ToolPartTarget(Set<String> targets) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return Set.of(TargetTypes.TOOL_PART_TYPE);
    }

    @Override
    public Set<String> getTags() {
        return targets;
    }
}
