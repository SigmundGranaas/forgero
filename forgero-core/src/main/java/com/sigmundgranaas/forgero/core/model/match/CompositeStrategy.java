package com.sigmundgranaas.forgero.core.model.match;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Typed;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Defines matching strategies for Composite and Constructed objects against given predicates.
 * Depending on the type of the input Matchable object, the appropriate strategy is applied.
 */
public class CompositeStrategy implements Matchable {
	private final List<Matchable> predicates;

	/**
	 * Anonymous class that provides the matching logic specific to Composite objects.
	 * The test method is overridden to specify this behavior.
	 */
	private final Matchable compositeStrategy = (match, context) -> {
		// Cast the Matchable object to Composite
		Composite composite = (Composite) match;

		// Call the private helper method to match against the predicates
		return matchAgainstPredicates(composite.slots(), context);
	};

	/**
	 * Anonymous class that provides the matching logic specific to Constructed objects.
	 * The test method is overridden to specify this behavior.
	 */
	private final Matchable constructedStrategy = (match, context) -> {
		// Cast the Matchable object to Constructed
		Constructed construct = (Constructed) match;

		// Call the private helper method to match against the predicates
		return matchAgainstPredicates(construct.parts(), context);
	};

	/**
	 * Constructor for CompositeStrategy class.
	 *
	 * @param predicates The list of predicates to match against.
	 */
	public CompositeStrategy(List<Matchable> predicates) {
		this.predicates = predicates;
	}

	/**
	 * Overridden method from Matchable interface to implement matching logic.
	 * Matches Composite or Constructed objects against the predicates.
	 *
	 * @param match   Matchable object.
	 * @param context MatchContext object.
	 * @return boolean Returns true if matches against all predicates.
	 */
	@Override
	public boolean test(Matchable match, MatchContext context) {
		// Prepare context
		var compositeContext = context;
		if (match instanceof Typed identifiable) {
			compositeContext = compositeContext.add(identifiable.type());
		}

		// Check the type of match and use appropriate strategy
		if (match instanceof Composite) {
			var compositeSuccess = compositeStrategy.test(match, compositeContext);
			if (compositeSuccess) {
				return true;
			}
			if (match instanceof Constructed) {
				return constructedStrategy.test(match, compositeContext);
			}
		}
		// If the match is neither Composite nor Constructed, return false
		return false;
	}

	/**
	 * Private helper method to match a collection of items against the predicates.
	 *
	 * @param items   List of Matchable items.
	 * @param context MatchContext object.
	 * @return boolean Returns true if all items match against the predicates.
	 */
	private boolean matchAgainstPredicates(List<? extends Matchable> items, MatchContext context) {
		List<Matchable> matches = new ArrayList<>();
		for (Matchable item : items) {
			for (Matchable predicate : predicates) {
				// If an item matches a predicate and is not already in the matches list, add it
				if (predicate.test(item, context) && !matches.contains(predicate)) {
					matches.add(predicate);
				}
			}
		}

		// If the size of matches equals the size of predicates, all items match against the predicates
		return matches.size() == predicates.size();
	}
}
