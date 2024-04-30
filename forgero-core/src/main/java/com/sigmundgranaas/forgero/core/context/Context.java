package com.sigmundgranaas.forgero.core.context;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class Context implements Matchable {

	public static final Codec<Context> CODEC = Codec.STRING.xmap(
			Context::of,
			Context::value
	);
	private final String context;

	public Context(String context) {
		this.context = context;
	}

	public static Context of(String string) {
		return new Context(string);
	}

	public String value() {
		return context;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return this.test(match);
	}

	@Override
	public boolean test(Matchable matchable) {
		return matchable instanceof Context context && context.value().equals(this.value());
	}

	@Override
	public String toString() {
		return "Context{" +
				"context='" + context + '\'' +
				'}';
	}
}
