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
    public boolean match(Matchable state) {
        return matchers.stream().anyMatch(matchers -> matchers.match(state));
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider) {
        return matchers.stream().sorted(ModelMatcher::comparator).map(matcher -> matcher.get(state, provider)).flatMap(Optional::stream).findFirst();
    }
}