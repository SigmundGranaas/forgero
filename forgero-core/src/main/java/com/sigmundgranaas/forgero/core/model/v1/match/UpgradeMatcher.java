package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * This class is responsible for testing matches of type UPGRADE.
 */
public class UpgradeMatcher extends StatefulMatcher {

	public UpgradeMatcher(List<JsonElement> predicates, PredicateFactory predicateFactory) {
		super(predicates, predicateFactory);
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Composite composite) {
			var compositeTypeMatch = context.add(composite.type());
			return composite.slots().stream().allMatch(slot -> predicates.stream().anyMatch(criteria -> testSlot(slot, criteria, compositeTypeMatch)));
		} else if (match instanceof FilledSlot slot) {
			return predicates.stream().allMatch(criteria -> testSlot(slot, criteria, context));
		}
		return false;
	}
}
