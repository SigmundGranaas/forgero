package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.List;
import java.util.Optional;

public class MultipleModelMatcher implements ModelMatcher {
    private final List<ModelMatcher> matchers;

    public MultipleModelMatcher(List<ModelMatcher> matchers) {
        this.matchers = matchers;
    }

    public static ModelMatcher of(List<ModelMatcher> matcher) {
        return new MultipleModelMatcher(matcher);
    }

    @Override
    public Optional<ModelTemplate> match(Matchable state, ModelProvider provider) {
        return matchers.stream().map(matcher -> matcher.match(state, provider)).flatMap(Optional::stream).findAny();
    }
}