package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.model.ModelResult.MODEL_RESULT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

public class MatchedModelEntry implements ModelMatcher {
	private final String id;
	private List<ModelMatchPairing> models;
	private final Map<String, List<ModelMatchPairing>> nameMatches;
	private final Map<Matchable, List<Pair<String, ModelMatchPairing>>> dynamicPredicates;
	private final List<ModelMatchPairing> others;


	public MatchedModelEntry(List<ModelMatchPairing> models, String id) {
		this.models = models;
		this.nameMatches = models.stream()
				.filter(this::hasNameMatches)
				.collect(Collectors.groupingBy(this::findFirstNameMatch));
		this.others = models.stream()
				.filter(entry -> !hasNameMatches(entry))
				.toList();
		this.id = id;
		this.dynamicPredicates = new ConcurrentHashMap<>();
	}

	private boolean hasNameMatches(ModelMatchPairing matchPairing) {
		return matchPairing.match().getPredicates().stream().anyMatch(pred -> pred instanceof NameMatch);
	}

	private String findFirstNameMatch(ModelMatchPairing matchPairing) {
		return matchPairing.match().getPredicates()
				.stream()
				.filter(pred -> pred instanceof NameMatch)
				.map(NameMatch.class::cast)
				.map(NameMatch::name)
				.findAny()
				.orElseThrow();
	}

	@Override
	public boolean match(Matchable match, MatchContext context) {
		return matchModels(match).stream().anyMatch(pair -> pair.match().test(match, context));
	}

	private List<ModelMatchPairing> matchModels(Matchable match) {
		if(others.isEmpty() && match instanceof MaterialBased based && nameMatches.containsKey(based.baseMaterial().name())){
			return nameMatches.get(based.baseMaterial().name());
		} else {
			return models;
		}
	}

	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		Optional<ModelTemplate> model = matchModels(state)
				.stream()
				.filter(pairing -> pairing.match().test(state, context))
				.sorted()
				.map(ModelMatchPairing::model)
				.map(pairing -> pairing.get(state, provider, context))
				.filter(Optional::isPresent)
				.flatMap(Optional::stream)
				.findFirst();

		if (model.isPresent()) {
			var modelOption = new InvalidationTracker();

			List<Pair<String, ModelMatchPairing>> uniqueModels = serializedPredicateList(state);

			if (!uniqueModels.isEmpty()) {
				context.get(MODEL_RESULT).ifPresent(result -> result.addOptions(modelOption));

				uniqueModels.forEach(uniquePair -> {
					Function<MatchContext, Boolean> fn = ctx -> uniquePair.getSecond().match().testDynamic(state, ctx);
					modelOption.addCheck(fn, context);
				});
			}
		}
		return model;
	}

	private List<Pair<String, ModelMatchPairing>> serializedPredicateList(Matchable match) {
		if(!this.dynamicPredicates.containsKey(match)){
			var entries = matchModels(match).stream()
					.filter(pair -> pair.match().getPredicates().stream().anyMatch(Matchable::isDynamic))
					.map(entry -> new Pair<>(serializePredicateList(entry.match().getDynamicPredicates()), entry))
					.toList();
			this.dynamicPredicates.put(match, entries);
		}
		return this.dynamicPredicates.get(match);
	}

	private String serializePredicateList(List<Matchable> predicates) {
		return predicates.stream()
				.map(Object::hashCode)
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}


	public void add(List<ModelMatchPairing> models) {
		this.models = ImmutableList.<ModelMatchPairing>builder().addAll(this.models).addAll(models).build();
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		return 0;
	}
}
