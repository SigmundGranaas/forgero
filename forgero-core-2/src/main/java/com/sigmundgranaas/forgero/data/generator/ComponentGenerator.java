package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;

/**
 * The ComponentGenerator is responsible for Stage 3 of the data pipeline.
 * It takes a normalized state and uses it to generate all combinatorial
 * items, such as parts from templates and materials, and tools from
 * templates and parts.
 */
public interface ComponentGenerator {
	/**
	 * Generates composite components from normalized templates and base definitions.
	 *
	 * @param state    The fully normalized state from Stage 2.
	 * @param tagGraph A graph of all tags, used for checking compatibility.
	 * @return A GeneratedState containing all newly created definitions.
	 */
	GeneratedState generate(NormalizedState state, TagGraph tagGraph);
}
