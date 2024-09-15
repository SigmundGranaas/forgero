package com.sigmundgranaas.forgero.tooltip.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.util.match.Matchable;


public class PredicateWriterFactory {
	private static final List<PredicateWriterBuilder> builders = new ArrayList<>();


	public static void register(PredicateWriterBuilder builder) {
		builders.add(builder);
	}


	public static void register(Supplier<PredicateWriterBuilder> builder) {
		builders.add(builder.get());
	}


	public Optional<PredicateWriter> build(Matchable element, TooltipConfiguration configuration) {
		return builders.stream()
				.map(builder -> builder.build(element, configuration))
				.flatMap(Optional::stream)
				.findAny();
	}
}
