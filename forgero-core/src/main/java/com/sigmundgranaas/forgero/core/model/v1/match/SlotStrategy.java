package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;


/**
 * This class provides a strategy for matching FilledSlot objects with a list of predicates.
 * Each predicate is a condition that the FilledSlot must meet in order for the strategy to be a match.
 */
public class SlotStrategy implements Matchable {
	private final List<Matchable> predicates;

	/**
	 * Create a new SlotStrategy with the given list of predicates.
	 *
	 * @param predicates The list of predicates that a FilledSlot must meet for this strategy to match.
	 */
	public SlotStrategy(List<Matchable> predicates) {
		this.predicates = predicates;
	}

	/**
	 * Test if a Matchable object satisfies the predicates specified in this strategy.
	 * The Matchable object should be an instance of FilledSlot for the predicates to be applied. If the FilledSlot
	 * object meets all the predicates, true is returned.
	 *
	 * @param match   The Matchable object, expected to be a FilledSlot instance.
	 * @param context The MatchContext to provide additional matching context, which is used if the FilledSlot does not exist.
	 * @return True if all predicates are satisfied by the FilledSlot object or by the context (when FilledSlot does not exist), otherwise false.
	 */
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof FilledSlot slot) {
			var matchesAllSlots = predicates.stream().allMatch(pred -> pred.test(slot, context));
			if (matchesAllSlots) {
				return true;
			}
			var slotContentOpt = slot.get();
			if (slotContentOpt.isPresent()) {
				return predicates.stream().allMatch(pred -> pred.test(slot, context));
			}
		}
		return false;
	}
}
