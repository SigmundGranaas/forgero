package com.sigmundgranaas.forgero.core.properties.attribute;

import java.util.Set;

public record TargetTag(Set<String> tags) {
    public boolean isApplicable(String tag) {
        return tags.contains(tag);
    }
}
