package com.sigmundgranaas.forgero.core.properties.attribute;

import java.util.Set;

public record TargetTagSet(Set<String> tags) implements TargetTag {
    public boolean isApplicable(String tag) {
        return tags.contains(tag);
    }
}
