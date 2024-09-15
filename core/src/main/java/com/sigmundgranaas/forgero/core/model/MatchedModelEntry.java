package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.model.ModelResult.MODEL_RESULT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class MatchedModelEntry implements ModelMatcher {
	private final String id;
	private List<ModelMatchPairing> models;

	public MatchedModelEntry(List<ModelMatchPairing> models, String id) {
		this.models = models;
		this.id = id;
	}

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return models.stream().anyMatch(pair -> pair.match().test(state, context));
	}

	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		Optional<ModelTemplate> model = models.stream()
				.filter(pairing -> pairing.match().test(state, context))
				.sorted()
				.map(ModelMatchPairing::model)
				.map(pairing -> pairing.get(state, provider, context))
				.filter(Optional::isPresent)
				.flatMap(Optional::stream)
				.findFirst();
		if (model.isPresent()) {
			var modelOption = new InvalidationTracker();

			Map<String, ModelMatchPairing> uniqueModels = new HashMap<>();

			models.stream()
					.filter(pair -> pair.match().getPredicates().stream().anyMatch(Matchable::isDynamic))
					.forEach(pair -> uniqueModels.put(serializePredicateList(pair.match().getDynamicPredicates()), pair));

			uniqueModels.values().forEach(uniquePair -> {
				Function<MatchContext, Boolean> fn = ctx -> uniquePair.match().testDynamic(state, ctx);
				modelOption.addCheck(fn, context);
			});
			if (!uniqueModels.isEmpty()) {
				context.get(MODEL_RESULT).ifPresent(result -> result.addOptions(modelOption));
			}
		}
		return model;
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
