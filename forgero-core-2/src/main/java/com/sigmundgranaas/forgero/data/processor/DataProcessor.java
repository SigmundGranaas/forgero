package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.data.definition.RawDefinition;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.Map;

/**
 * The DataProcessor is responsible for Stage 2 of the data pipeline.
 * It transforms raw, unprocessed definitions into a "normalized" state
 * by resolving all `include` directives and merging properties.
 */
public interface DataProcessor {
	/**
	 * Processes a collection of raw definitions, resolving all `include`
	 * dependencies and returning a complete, normalized state.
	 *
	 * @param rawDefinitions A map of raw definitions loaded from files.
	 * @return An immutable NormalizedState containing all processed definitions.
	 * @throws IllegalArgumentException if an included ID refers to a non-existent definition.
	 * @throws IllegalStateException    if a cyclic include dependency is detected.
	 */
	NormalizedState normalize(Map<OpenIdentifier, RawDefinition> rawDefinitions);
}
