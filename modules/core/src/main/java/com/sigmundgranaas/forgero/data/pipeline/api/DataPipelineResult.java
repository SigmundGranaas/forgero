package com.sigmundgranaas.forgero.data.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.validation.ValidationResult;

import java.util.List;
import java.util.Map;

/**
 * Intermediate result of the data pipeline, exposing phase results for validation.
 * <p>
 * This provides visibility into each stage of the pipeline:
 * <ol>
 *   <li>Raw definitions loaded from JSON files</li>
 *   <li>Template expansion results (how many components each template generated)</li>
 *   <li>Final data bundle with all components</li>
 *   <li>Any parsing errors encountered during loading</li>
 * </ol>
 */
public record DataPipelineResult(
		/**
		 * Phase 1: Raw definitions loaded from JSON files (before template expansion).
		 * Includes materials, schematics, static parts, and templates.
		 */
		Map<OpenIdentifier, RawDefinition> rawDefinitions,

		/**
		 * Phase 2: Template expansion results showing how many components
		 * each template generated. Critical for validation - templates with
		 * zero results indicate configuration errors.
		 */
		TemplateExpansionResult templateExpansion,

		/**
		 * Phase 3: Final bundle containing all processed components.
		 */
		ForgeroDataBundle bundle,

		/**
		 * Parsing errors collected during loading.
		 * These are errors that prevented a resource from being loaded.
		 */
		List<ParsingError> parsingErrors,

		/**
		 * Phase 4: Component validation results.
		 * Contains errors and warnings from structural, reference, and attribute validation.
		 */
		ValidationResult validationResult
) {
	/**
	 * @return true if any parsing errors occurred
	 */
	public boolean hasParsingErrors() {
		return !parsingErrors.isEmpty();
	}

	/**
	 * @return true if any template produced zero results
	 */
	public boolean hasEmptyTemplates() {
		return templateExpansion != null && templateExpansion.hasEmptyTemplates();
	}

	/**
	 * @return true if any validation errors occurred
	 */
	public boolean hasValidationErrors() {
		return validationResult != null && validationResult.hasErrors();
	}

	/**
	 * @return true if any validation warnings occurred
	 */
	public boolean hasValidationWarnings() {
		return validationResult != null && validationResult.hasWarnings();
	}

	/**
	 * Creates a result for cases where the pipeline failed early.
	 */
	public static DataPipelineResult failed(List<ParsingError> errors) {
		return new DataPipelineResult(
				Map.of(),
				TemplateExpansionResult.empty(),
				null,
				errors,
				ValidationResult.empty()
		);
	}
}
