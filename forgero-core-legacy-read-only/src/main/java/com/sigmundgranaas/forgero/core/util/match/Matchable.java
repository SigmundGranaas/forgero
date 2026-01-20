package com.sigmundgranaas.forgero.core.util.match;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;

@FunctionalInterface
public interface Matchable {
	Matchable DEFAULT_FALSE = (match, context) -> false;
	Matchable DEFAULT_TRUE = (match, context) -> true;

	Codec<Matchable> CODEC = Codec.of(
			Encoder.<Matchable>empty().encoder(),
			Decoder.unit(DEFAULT_TRUE).decoder()
	);


	boolean test(Matchable match, MatchContext context);

	default boolean test(Matchable matchable) {
		return test(matchable, MatchContext.of());
	}

	default boolean isDynamic() {
		return false;
	}
}
