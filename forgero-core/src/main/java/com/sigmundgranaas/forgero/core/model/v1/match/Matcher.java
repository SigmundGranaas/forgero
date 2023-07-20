package com.sigmundgranaas.forgero.core.model.v1.match;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.model.ModelMatchEntry;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;

/**
 * Abstract class responsible for holding the common behavior and attributes for Matchers.
 */
public abstract class Matcher implements Matchable {
	protected final List<Predicate<MatchContext>> predicates;

	/**
	 * @param criteria         The list of criteria to match against.
	 * @param predicateFactory The factory to create predicates.
	 */
	public Matcher(List<JsonObject> criteria, ModelPredicateFactory predicateFactory) {
		this.predicates = criteria.stream()
				.map(predicateFactory::create)
				.collect(Collectors.toList());
	}

	protected boolean testSlot(Slot upgrade, Predicate<MatchContext> predicate, MatchContext context) {
		if (upgrade.filled()) {
			if (criteria.contains("slot:")) {
				return criteria.replace("slot:", "").equals(String.valueOf(upgrade.index()));
			} else if (criteria.contains("type")) {
				var type = Type.of(criteria.replace("type:", ""));
				if (context.test(type, MatchContext.of())) {
					return true;
				}
				return upgrade.test(Type.of(criteria.replace("type:", "")), context);
			}
			return upgrade.test(new NameMatch(criteria), context);

		}
		return false;
	}

	private boolean ingredientTest(State ingredient, Predicate<MatchContext> predicate, MatchContext context) {
		if (criteria.contains("type")) {
			var type = Type.of(criteria.replace("type:", ""));
			if (context.test(type, MatchContext.of())) {
				return true;
			}
			return ingredient.test(type, context);
		} else if (criteria.contains("id")) {
			return ingredient.identifier().split(":")[1].equals(criteria.replace("id:", ""));
		} else if (criteria.contains("model")) {
			return context.test(new ModelMatchEntry(criteria.replace("model:", "")), context);
		} else if (ingredient.name().split(ELEMENT_SEPARATOR)[0].equals(criteria)) {
			return true;
		}
		return ingredient.test(new NameMatch(criteria), context);
	}
}
