package com.sigmundgranaas.forgero.core.api.identity;

import java.util.function.Function;

import com.sigmundgranaas.forgero.core.state.State;

/**
 * Modification rules are used to modify the names of States that are used to construct another State.
 * An example is this: Pickaxe head + handle = Pickaxe.
 * In this case, we want to deterministically resolve a valid id based on the parts given.
 * Modification rules modify the names of elements in the construct to compose valid id's.
 */
public interface ModificationRule {

	/**
	 * Whether the rule applies to the given state
	 *
	 * @param state The state to test
	 * @return true if it should apply, false otherwise
	 */
	boolean applies(State state);

	/**
	 * The transformation which should be applied to the current name.
	 * Usually removals or replacements, or by default the identity function.
	 * <p>
	 * An empty response: "" is ignored.
	 *
	 * @return The function that will transform the name
	 */
	Function<String, String> transformation();
}
