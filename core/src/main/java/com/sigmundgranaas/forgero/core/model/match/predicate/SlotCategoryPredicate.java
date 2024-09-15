package com.sigmundgranaas.forgero.core.model.match.predicate;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Matches if the category of the slot matches to the given category and the slot is filled.
 */
public record SlotCategoryPredicate(Category category) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		Optional<Slot> slotOpt = context.get(Slot.SLOT_CONTEXT_KEY);
		Slot slot = null;
		if (match instanceof Slot slotInstance) {
			slot = slotInstance;
		} else if (slotOpt.isPresent()) {
			slot = slotOpt.get();
		}
		return slot != null && slot.filled() && slot.category().contains(category);
	}
}
