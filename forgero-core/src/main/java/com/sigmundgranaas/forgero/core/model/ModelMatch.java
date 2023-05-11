package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;

public record ModelMatch(List<String> criteria, String matchType) implements Matchable {
	@Override
	public boolean test(Matchable match, Context context) {
		if (match instanceof Composite composite) {
			var compositeMatch = context.add(composite.type());
			if (matchType.equals("UPGRADE")) {
				return composite.slots().stream().allMatch(slot -> criteria.stream().anyMatch(criteria -> upgradeTest(slot, criteria, compositeMatch)));
			} else if (match instanceof Constructed construct) {
				List<String> matches = new ArrayList<>();
				for (State ingredient : construct.parts()) {
					for (String criteria : criteria) {
						if (ingredientTest(ingredient, criteria, context)) {
							if (!matches.contains(criteria)) {
								matches.add(criteria);
							}
						}
					}
				}
				return matches.size() == criteria.size();
			} else if (criteria.size() == 1) {
				if (ingredientTest(composite, criteria.get(0), context)) {
					return true;
				} else if (match instanceof Constructed construct) {
					return construct.parts().stream().anyMatch(ingredient -> ingredientTest(ingredient, criteria.get(0), compositeMatch));
				}
			}
		} else if (match instanceof State state) {
			return criteria.stream().allMatch(criteria -> ingredientTest(state, criteria, context));
		} else if (match instanceof FilledSlot slot) {
			if (matchType.equals("UPGRADE")) {
				return criteria.stream().allMatch(criteria -> upgradeTest(slot, criteria, context));
			} else if (slot.get().isPresent() && criteria.size() == 1) {
				if (ingredientTest(slot.get().get(), criteria.get(0), context)) {
					return true;
				}
				return slot.get().get().name().equals(criteria.get(0));
			} else if (slot.get().isPresent()) {
				return criteria.stream().allMatch(criteria -> ingredientTest(slot.get().get(), criteria, context));
			}
		}
		return false;
	}

	private boolean ingredientTest(State ingredient, String criteria, Context context) {
		if (criteria.contains("type")) {
			var type = Type.of(criteria.replace("type:", ""));
			if (context.test(type, Context.of())) {
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

	private boolean upgradeTest(Slot upgrade, String criteria, Context context) {
		if (upgrade.filled()) {
			if (criteria.contains("slot:")) {
				return criteria.replace("slot:", "").equals(String.valueOf(upgrade.index()));
			} else if (criteria.contains("type")) {
				var type = Type.of(criteria.replace("type:", ""));
				if (context.test(type, Context.of())) {
					return true;
				}
				return upgrade.test(Type.of(criteria.replace("type:", "")), context);
			}
			return upgrade.test(new NameMatch(criteria), context);

		}
		return false;
	}
}
