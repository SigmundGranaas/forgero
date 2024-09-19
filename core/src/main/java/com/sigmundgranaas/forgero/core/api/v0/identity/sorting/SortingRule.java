package com.sigmundgranaas.forgero.core.api.v0.identity.sorting;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

/**
 * Sorting rules are used to sort states in the correct order when trying to construct parts and tools.
 * An example is Head + Handle = Tool, or material + schematic = part
 * The sorting rules would make sure that the materials always comes before the schematic to ensure correct ordering when trying to create an id.
 */
public interface SortingRule {
	static SortingRule of(Type type, int priority) {
		return new TypeRule(type, priority);
	}

	/**
	 * Whether the rule applies to the given state
	 *
	 * @param state The state to test
	 * @return true if it should apply, false otherwise
	 */
	boolean applies(State state);

	/**
	 * The priority to use when comparing items.
	 * Lower numbers will place it ahead in the collection
	 *
	 * @return the integer to use for the comparator
	 */
	int getPriority();
}
