package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class PredicateFactory {
	private static final List<PredicateBuilder> builders = new ArrayList<>();

	public static void register(PredicateBuilder builder) {
		builders.add(builder);
	}

	public Matchable create(JsonElement element) {
		return builders.stream()
				.map(builder -> builder.create(element))
				.flatMap(Optional::stream)
				.findAny()
				.orElseGet(() -> {
					Forgero.LOGGER.error("Found predicate element with no corresponding predicate builder: {}, the corresponding entry will always fail matching checks.", element);
					return Matchable.DEFAULT_FALSE;
				});
	}
}
