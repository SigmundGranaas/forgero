package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;

import java.util.Map;

/**
 * The DataProcessor is responsible for resolving dependencies and normalizing raw Forgero data DTOs.
 * Specifically, it handles the `include` mechanism, merging properties from included definitions
 * into the main definition, with overriding by ID for attributes and by type for features.
 * This effectively creates a fully self-contained, "normalized" representation of each data entry.
 *
 * This is Stage 2 in the proposed data loading pipeline.
 */
public interface DataProcessor {
	/**
	 * Processes a collection of raw Forgero data definitions, resolving all `include`
	 * dependencies and returning a map of fully processed and normalized definitions.
	 *
	 * @param rawData A map where keys are the canonical OpenIdentifiers of the data entries
	 *                (e.g., `forgero:iron`) and values are the raw DTOs parsed from files.
	 * @return An immutable map of OpenIdentifier to the processed TopLevelData DTOs.
	 * @throws IllegalArgumentException if an included ID refers to a non-existent definition.
	 * @throws IllegalStateException if a cyclic include dependency is detected.
	 */
	Map<OpenIdentifier, TopLevelData> process(Map<OpenIdentifier, TopLevelData> rawData);
}
