package com.sigmundgranaas.forgero.core.model;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class MultipleModelMatcher implements ModelMatcher {
	private final List<ModelMatcher> matchers;

	public MultipleModelMatcher(List<ModelMatcher> matchers) {
		this.matchers = matchers;
	}

	public static ModelMatcher of(List<ModelMatcher> matcher) {
		return new MultipleModelMatcher(matcher);
	}

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return matchers.stream().anyMatch(matchers -> matchers.match(state, context));
	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		return matchers.stream()
				.filter(modelMatcher -> modelMatcher.match(state, context))
				.sorted(ModelMatcher::comparator)
				.map(matcher -> matcher.get(state, provider, context))
				.flatMap(Optional::stream)
				.findFirst();
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		return ModelMatcher.super.compareTo(o);
	}
}
