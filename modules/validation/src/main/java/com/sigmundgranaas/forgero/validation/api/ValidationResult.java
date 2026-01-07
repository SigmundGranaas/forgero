package com.sigmundgranaas.forgero.validation.api;

import com.sigmundgranaas.forgero.data.pipeline.api.ParsingError;
import com.sigmundgranaas.forgero.data.pipeline.api.TemplateExpansionResult;
import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult;
import com.sigmundgranaas.forgero.model.validation.api.ModelValidationResult;
import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult;
import com.sigmundgranaas.forgero.model.validation.api.TextureValidationResult;

import java.util.List;

/**
 * Aggregated result of content pack validation.
 * <p>
 * Contains results from all validation phases:
 * <ol>
 *   <li>Definition parsing (JSON syntax and codec errors)</li>
 *   <li>Template expansion (components generated per template)</li>
 *   <li>Model validation (JSON syntax, references)</li>
 *   <li>Texture validation (grayscale templates, palettes)</li>
 *   <li>Palette conformity (strict size rules)</li>
 *   <li>Animated texture validation (mcmeta files)</li>
 * </ol>
 *
 * <h3>Error Severity</h3>
 * <ul>
 *   <li><b>CRITICAL</b>: Empty templates - indicates broken configuration</li>
 *   <li><b>ERROR</b>: Parsing failures, invalid JSON, broken references, invalid palettes</li>
 *   <li><b>WARNING</b>: Missing optional resources, deprecated patterns, non-standard palettes</li>
 * </ul>
 */
public record ValidationResult(
		/**
		 * Definition parsing results (JSON syntax and codec errors).
		 */
		DefinitionValidationResult definitionResult,

		/**
		 * Template expansion results with per-template statistics.
		 */
		TemplateExpansionResult templateResult,

		/**
		 * Model JSON validation results (null if models not validated).
		 */
		ModelValidationResult modelResult,

		/**
		 * Texture validation results (null if textures not validated).
		 */
		TextureValidationResult textureResult,

		/**
		 * Palette conformity validation results (null if palettes not validated).
		 */
		PaletteConformityResult paletteResult,

		/**
		 * Animated texture validation results (null if animations not validated).
		 */
		AnimatedTextureResult animatedTextureResult,

		/**
		 * Overall validation statistics.
		 */
		Statistics statistics
) {
	/**
	 * @return true if any critical errors were found (empty templates, parsing errors)
	 */
	public boolean hasErrors() {
		return hasCriticalErrors() 
				|| hasParsingErrors() 
				|| hasModelErrors() 
				|| hasTextureErrors()
				|| hasPaletteErrors()
				|| hasAnimatedTextureErrors();
	}

	/**
	 * @return true if any templates produced zero results (CRITICAL)
	 */
	public boolean hasCriticalErrors() {
		return templateResult != null && templateResult.hasEmptyTemplates();
	}

	/**
	 * @return true if any parsing errors occurred
	 */
	public boolean hasParsingErrors() {
		return definitionResult != null && definitionResult.hasErrors();
	}

	/**
	 * @return true if any model validation errors occurred
	 */
	public boolean hasModelErrors() {
		return modelResult != null && modelResult.hasErrors();
	}

	/**
	 * @return true if any texture validation errors occurred
	 */
	public boolean hasTextureErrors() {
		return textureResult != null && textureResult.hasErrors();
	}

	/**
	 * @return true if any palette conformity errors occurred
	 */
	public boolean hasPaletteErrors() {
		return paletteResult != null && paletteResult.hasErrors();
	}

	/**
	 * @return true if any animated texture errors occurred
	 */
	public boolean hasAnimatedTextureErrors() {
		return animatedTextureResult != null && animatedTextureResult.hasErrors();
	}

	/**
	 * @return Total number of errors across all validation phases
	 */
	public int errorCount() {
		int count = 0;
		if (templateResult != null) {
			count += templateResult.emptyTemplates().size();
		}
		if (definitionResult != null) {
			count += definitionResult.errors().size();
		}
		if (modelResult != null) {
			count += modelResult.errors().size();
		}
		if (textureResult != null) {
			count += textureResult.errors().size();
		}
		if (paletteResult != null) {
			count += paletteResult.errorCount();
		}
		if (animatedTextureResult != null) {
			count += animatedTextureResult.errorCount();
		}
		return count;
	}

	/**
	 * @return Total number of warnings across all validation phases
	 */
	public int warningCount() {
		int count = 0;
		if (templateResult != null) {
			count += templateResult.warnings().size();
		}
		if (definitionResult != null) {
			count += definitionResult.warnings().size();
		}
		if (modelResult != null) {
			count += modelResult.warnings().size();
		}
		if (textureResult != null) {
			count += textureResult.warnings().size();
		}
		if (paletteResult != null) {
			count += paletteResult.warningCount();
		}
		if (animatedTextureResult != null) {
			count += animatedTextureResult.warningCount();
		}
		return count;
	}

	/**
	 * Creates an empty result for cases where validation couldn't start.
	 */
	public static ValidationResult empty() {
		return new ValidationResult(
				DefinitionValidationResult.empty(),
				TemplateExpansionResult.empty(),
				null,
				null,
				null,
				null,
				new Statistics(0, 0, 0, 0, 0)
		);
	}

	/**
	 * Creates a failed result for early failures.
	 */
	public static ValidationResult failed(List<ParsingError> errors) {
		return new ValidationResult(
				new DefinitionValidationResult(errors, List.of(), 0),
				TemplateExpansionResult.empty(),
				null,
				null,
				null,
				null,
				new Statistics(0, 0, 0, 0, 0)
		);
	}

	/**
	 * Validation statistics.
	 */
	public record Statistics(
			int definitionsLoaded,
			int templatesProcessed,
			int componentsGenerated,
			int modelsValidated,
			int texturesValidated
	) {
	}
}
