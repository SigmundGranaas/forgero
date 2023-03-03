package com.sigmundgranaas.forgero.core.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MatchedModelEntry implements ModelMatcher {
	private List<ModelMatchPairing> models;
	private String id;

	public MatchedModelEntry(List<ModelMatchPairing> models, String id) {
		this.models = models;
		this.id = id;
	}

	@Override
	public boolean match(Matchable state, Context context) {
		return models.stream().anyMatch(pair -> pair.match().test(state, context));

	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context) {
		return models.stream()
				.filter(pairing -> pairing.match().test(state, context))
				.sorted()
				.map(ModelMatchPairing::model)
				.map(pairing -> pairing.get(state, provider, context))
				.filter(Optional::isPresent)
				.flatMap(Optional::stream)
				.findFirst();
	}

	public void add(List<ModelMatchPairing> models) {
		this.models = ImmutableList.<ModelMatchPairing>builder().addAll(this.models).addAll(models).build();
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		return 0;
	}
}
