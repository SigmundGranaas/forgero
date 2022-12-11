package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.property.TargetTypes;
import com.sigmundgranaas.forgero.type.Type;

import java.util.Set;

public record TypeTarget(Set<String> targets) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return Set.of(TargetTypes.TYPE);
    }

    @Override
    public Set<String> getTags() {
        return targets;
    }

    @Override
    public boolean isApplicable(Set<String> tag, TargetTypes type) {
        if (getTypes().contains(type)) {
            return tag.stream().map(this::toTag).anyMatch(tagged -> targets.stream().map(this::toTag).anyMatch(target -> tagged.test(target)));
        }
        return false;
    }

    private Type toTag(String tag) {
        return ForgeroStateRegistry.TREE.type(tag);
    }
}