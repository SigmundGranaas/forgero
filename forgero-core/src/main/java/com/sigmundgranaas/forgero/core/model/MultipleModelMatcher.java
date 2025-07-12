package com.sigmundgranaas.forgero.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class MultipleModelMatcher implements ModelMatcher {
	private final List<ModelMatcher> matchers;
	private final Map<String, List<ModelMatcher>> matches = new ConcurrentHashMap<>();
	private final Map<String, ModelTemplate> exactMatch = new ConcurrentHashMap<>();


	public MultipleModelMatcher(List<ModelMatcher> matchers) {
		this.matchers = matchers;
	}

	public static ModelMatcher of(List<ModelMatcher> matcher) {
		return new MultipleModelMatcher(matcher);
	}

	@Override
	public boolean match(Matchable matchable, MatchContext context) {
		if(matchable instanceof State state && matches.containsKey(state.identifier())){
			return true;
		}else{
			return matchers.stream().anyMatch(matchers -> matchers.match(matchable, context));
		}
	}

	private List<ModelMatcher> getMatchesForState(Matchable matchable){
		if(matchable instanceof State state){
			if(!matches.containsKey(state.identifier())){
				var models = matchers.stream()
						.filter(modelMatcher -> modelMatcher.match(state, MatchContext.of()))
						.sorted(ModelMatcher::comparator)
						.toList();
				matches.put(state.identifier(), models);
			}
			return matches.get(state.identifier());
		}else{
		return 	matchers.stream()
					.filter(modelMatcher -> modelMatcher.match(matchable, MatchContext.of()))
					.sorted(ModelMatcher::comparator)
					.toList();
		}
	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		if(state instanceof State state1 && exactMatch.containsKey(state1.identifier())){
			return Optional.of(exactMatch.get(state1.identifier()));
		}

		var matches = getMatchesForState(state);

		Optional<ModelTemplate> result = matches.stream()
				.map(matcher -> matcher.get(state, provider, context))
				.flatMap(Optional::stream)
				.findFirst();

		if(matches.size() == 1 && result.isPresent() && state instanceof State state1){
			exactMatch.put(state1.identifier(), result.get());
		}

		if(matches.isEmpty()){
			Forgero.LOGGER.warn("Encountered matchable with no valid model templates. Matchable in question: {}", state);
		}

		return result;
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		return ModelMatcher.super.compareTo(o);
	}
}
