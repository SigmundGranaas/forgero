package com.sigmundgranaas.forgero.validation.api;

import com.sigmundgranaas.forgero.data.pipeline.api.ParsingError;

import java.util.List;

/**
 * Result of definition parsing validation.
 * <p>
 * Captures errors and warnings from loading JSON definition files
 * and parsing them through the codec system.
 */
public record DefinitionValidationResult(
		/**
		 * Parsing errors (JSON syntax errors, codec failures, missing required fields).
		 */
		List<ParsingError> errors,

		/**
		 * Parsing warnings (deprecated fields, ignored properties, etc.).
		 */
		List<DefinitionWarning> warnings,

		/**
		 * Total number of definitions successfully loaded.
		 */
		int definitionsLoaded
) {
	/**
	 * @return true if any parsing errors occurred
	 */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/**
	 * Warning about a definition that didn't prevent loading but may indicate issues.
	 */
	public record DefinitionWarning(
			String source,
			String message
	) {
	}

	/**
	 * Creates an empty result for cases where no definitions were processed.
	 */
	public static DefinitionValidationResult empty() {
		return new DefinitionValidationResult(List.of(), List.of(), 0);
	}
}
