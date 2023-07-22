package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class CompositeStrategy implements Matchable {
	private final List<Matchable> predicates;

	public CompositeStrategy(List<Matchable> predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Composite composite) {
			var compositeMatch = context.add(composite.type());

			List<Matchable> slotMatches = new ArrayList<>();
			for (Slot slot : composite.slots()) {
				for (Matchable predicate : predicates) {
					if (predicate.test(slot, compositeMatch)) {
						if (!slotMatches.contains(predicate)) {
							slotMatches.add(predicate);
						}
					}
				}
			}
			if (slotMatches.size() == predicates.size()) {
				return true;
			}


			if (match instanceof Constructed construct) {
				List<Matchable> matches = new ArrayList<>();
				for (State ingredient : construct.parts()) {
					for (Matchable predicate : predicates) {
						if (predicate.test(ingredient, compositeMatch)) {
							if (!matches.contains(predicate)) {
								matches.add(predicate);
							}
						}
					}
				}
				return matches.size() == predicates.size();

			}
		}
		return false;
	}
}
