package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

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
    public boolean match(Matchable state, Context context) {
        return matchers.stream().anyMatch(matchers -> matchers.match(state, context));
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context) {
        return matchers.stream().filter(modelMatcher -> modelMatcher.match(state, context)).sorted(ModelMatcher::comparator).sorted().map(matcher -> matcher.get(state, provider, context)).flatMap(Optional::stream).findFirst();
    }

    @Override
    public int compareTo(@NotNull ModelMatcher o) {
        return 0;
    }
}