package com.sigmundgranaas.forgero.core.pattern;

import java.util.List;

public class PatternCollection {
    private final List<Pattern> patterns;

    public PatternCollection(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }
}
