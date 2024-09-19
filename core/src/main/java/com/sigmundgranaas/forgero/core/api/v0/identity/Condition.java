package com.sigmundgranaas.forgero.core.api.v0.identity;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

/**
 * Condition class used to build dynamic conditions for States.
 * Aimed at making it easier to build Modification rules
 *
 * @param predicate
 */
public record Condition(Predicate<State> predicate) {
	public static Condition ALWAYS = of(e -> true);

	// Combines this condition with another using logical AND, creating a new Condition
	public Condition and(Condition other) {
		return new Condition(this.predicate.and(other.predicate));
	}

	// Combines this condition with another using logical OR, creating a new Condition
	public Condition or(Condition other) {
		return new Condition(this.predicate.or(other.predicate));
	}

	// Static factory methods for creating base conditions
	public static Condition nameContains(String substring) {
		return new Condition(state -> state.name().contains(substring));
	}

	public static Condition type(String type) {
		return new Condition(state -> state.type().test(Type.of(type), MatchContext.of()));
	}

	public static Condition of(Predicate<State> predicate) {
		return new Condition(predicate);
	}

	public static Condition type(Type type) {
		return new Condition(state -> state.type().test(type, MatchContext.of()));
	}
}
