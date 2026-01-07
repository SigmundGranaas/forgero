package com.sigmundgranaas.forgero.validation.api;

import com.sigmundgranaas.forgero.validation.impl.ContentPackValidatorBuilder;

import java.nio.file.Path;
import java.util.List;

/**
 * Main entry point for validating Forgero content packs.
 * <p>
 * Validates:
 * <ul>
 *   <li>JSON syntax and codec parsing for all definitions</li>
 *   <li>Tag references and cycles</li>
 *   <li>Template expansion results (CRITICAL: templates with 0 results cause failure)</li>
 *   <li>Model JSON files</li>
 *   <li>Texture resources (grayscale templates, palette dimensions)</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * ValidationResult result = ContentPackValidator.builder()
 *     .contentPaths(contentPaths)
 *     .defaultNamespace("forgero")
 *     .validateModels(true)
 *     .validateTextures(true)
 *     .build()
 *     .validate();
 *
 * if (result.hasErrors()) {
 *     System.err.println(result.formatReport());
 *     System.exit(1);
 * }
 * }</pre>
 */
public interface ContentPackValidator {

	/**
	 * Runs validation on the configured content packs.
	 *
	 * @return Aggregated validation result with errors, warnings, and statistics
	 */
	ValidationResult validate();

	/**
	 * Creates a new builder for configuring the validator.
	 *
	 * @return A new builder instance
	 */
	static Builder builder() {
		return new ContentPackValidatorBuilder();
	}

	/**
	 * Builder interface for configuring the ContentPackValidator.
	 */
	interface Builder {
		/**
		 * Sets the paths to content pack directories to validate.
		 * Each path should point to a content pack root containing
		 * src/main/resources/data/{namespace}/.
		 *
		 * @param paths Content pack root directories
		 * @return this builder
		 */
		Builder contentPaths(List<Path> paths);

		/**
		 * Sets the default namespace for identifiers.
		 *
		 * @param namespace The default namespace (e.g., "forgero")
		 * @return this builder
		 */
		Builder defaultNamespace(String namespace);

		/**
		 * Enables or disables model JSON validation.
		 *
		 * @param validate true to validate models, false to skip
		 * @return this builder
		 */
		Builder validateModels(boolean validate);

		/**
		 * Enables or disables texture resource validation.
		 *
		 * @param validate true to validate textures, false to skip
		 * @return this builder
		 */
		Builder validateTextures(boolean validate);

		/**
		 * Sets whether empty templates should cause validation failure.
		 * Default is true (recommended - empty templates are configuration errors).
		 *
		 * @param strict true to fail on empty templates, false to warn only
		 * @return this builder
		 */
		Builder strictTemplateValidation(boolean strict);

		/**
		 * Builds the configured validator.
		 *
		 * @return A configured ContentPackValidator instance
		 * @throws IllegalStateException if required configuration is missing
		 */
		ContentPackValidator build();
	}
}
