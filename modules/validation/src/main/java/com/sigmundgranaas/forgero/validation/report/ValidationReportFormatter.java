package com.sigmundgranaas.forgero.validation.report;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.pipeline.api.ParsingError;
import com.sigmundgranaas.forgero.data.pipeline.api.TemplateExpansionResult;
import com.sigmundgranaas.forgero.model.validation.api.*;
import com.sigmundgranaas.forgero.validation.api.DefinitionValidationResult;
import com.sigmundgranaas.forgero.validation.api.ValidationResult;

/**
 * Formats validation results for console output.
 * <p>
 * Produces human-readable reports with:
 * <ul>
 *   <li>Summary statistics</li>
 *   <li>Critical errors (empty templates)</li>
 *   <li>Parsing errors</li>
 *   <li>Model validation errors</li>
 *   <li>Texture validation errors</li>
 *   <li>Palette conformity errors</li>
 *   <li>Animated texture errors</li>
 *   <li>Warnings</li>
 * </ul>
 */
public class ValidationReportFormatter {

	private static final String SEPARATOR = "=".repeat(80);
	private static final String SECTION_SEPARATOR = "-".repeat(80);

	/**
	 * Formats a validation result into a human-readable report.
	 *
	 * @param result The validation result to format
	 * @return Formatted report string
	 */
	public String format(ValidationResult result) {
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\n").append(SEPARATOR).append("\n");
		sb.append("                    FORGERO CONTENT PACK VALIDATION REPORT\n");
		sb.append(SEPARATOR).append("\n\n");

		// Summary
		formatSummary(sb, result);

		// Critical Errors (Empty Templates)
		if (result.hasCriticalErrors()) {
			formatEmptyTemplates(sb, result.templateResult());
		}

		// Parsing Errors
		if (result.hasParsingErrors()) {
			formatParsingErrors(sb, result.definitionResult());
		}

		// Model Errors
		if (result.hasModelErrors()) {
			formatModelErrors(sb, result.modelResult());
		}

		// Texture Errors
		if (result.hasTextureErrors()) {
			formatTextureErrors(sb, result.textureResult());
		}

		// Palette Conformity Errors
		if (result.paletteResult() != null && result.paletteResult().hasErrors()) {
			formatPaletteErrors(sb, result.paletteResult());
		}

		// Animated Texture Errors
		if (result.animatedTextureResult() != null && result.animatedTextureResult().hasErrors()) {
			formatAnimatedTextureErrors(sb, result.animatedTextureResult());
		}

		// Warnings
		if (result.warningCount() > 0) {
			formatWarnings(sb, result);
		}

		// Footer
		sb.append(SEPARATOR).append("\n");
		if (result.hasErrors()) {
			sb.append("VALIDATION FAILED - ").append(result.errorCount()).append(" error(s) found\n");
		} else {
			sb.append("VALIDATION PASSED - No errors found\n");
		}
		sb.append(SEPARATOR).append("\n");

		return sb.toString();
	}

	private void formatSummary(StringBuilder sb, ValidationResult result) {
		ValidationResult.Statistics stats = result.statistics();

		sb.append("SUMMARY\n");
		sb.append(SECTION_SEPARATOR).append("\n");
		sb.append(String.format("  Definitions loaded:     %d\n", stats.definitionsLoaded()));
		sb.append(String.format("  Templates processed:    %d\n", stats.templatesProcessed()));
		sb.append(String.format("  Components generated:   %d\n", stats.componentsGenerated()));
		sb.append(String.format("  Models validated:       %d\n", stats.modelsValidated()));
		sb.append(String.format("  Textures validated:     %d\n", stats.texturesValidated()));
		
		// Add palette and animation stats if available
		if (result.paletteResult() != null) {
			sb.append(String.format("  Palettes validated:     %d\n", result.paletteResult().totalPalettesValidated()));
		}
		if (result.animatedTextureResult() != null) {
			sb.append(String.format("  Animated textures:      %d\n", result.animatedTextureResult().totalAnimatedTextures()));
		}
		
		sb.append("\n");
		sb.append(String.format("  Total errors:           %d\n", result.errorCount()));
		sb.append(String.format("  Total warnings:         %d\n", result.warningCount()));
		sb.append("\n");
	}

	private void formatEmptyTemplates(StringBuilder sb, TemplateExpansionResult templateResult) {
		sb.append("CRITICAL ERRORS - EMPTY TEMPLATES\n");
		sb.append(SECTION_SEPARATOR).append("\n");
		sb.append("The following templates produced 0 results. This indicates a configuration error:\n");
		sb.append("  - Missing materials matching required tags\n");
		sb.append("  - Typo in tag references\n");
		sb.append("  - Missing content pack dependency\n\n");

		for (OpenIdentifier template : templateResult.emptyTemplates()) {
			sb.append("  [CRITICAL] ").append(template).append("\n");
		}
		sb.append("\n");
	}

	private void formatParsingErrors(StringBuilder sb, DefinitionValidationResult definitionResult) {
		sb.append("PARSING ERRORS\n");
		sb.append(SECTION_SEPARATOR).append("\n");

		for (ParsingError error : definitionResult.errors()) {
			sb.append("  [ERROR] ").append(error.source()).append("\n");
			sb.append("          ").append(error.message()).append("\n");
			if (error.cause() != null) {
				sb.append("          Cause: ").append(error.cause().getClass().getSimpleName())
						.append(": ").append(error.cause().getMessage()).append("\n");
			}
			sb.append("\n");
		}
	}

	private void formatModelErrors(StringBuilder sb, ModelValidationResult modelResult) {
		sb.append("MODEL VALIDATION ERRORS\n");
		sb.append(SECTION_SEPARATOR).append("\n");

		for (ModelValidationResult.ModelError error : modelResult.errors()) {
			sb.append("  [").append(error.type()).append("] ").append(error.modelId()).append("\n");
			sb.append("          ").append(error.message()).append("\n\n");
		}
	}

	private void formatTextureErrors(StringBuilder sb, TextureValidationResult textureResult) {
		sb.append("TEXTURE VALIDATION ERRORS\n");
		sb.append(SECTION_SEPARATOR).append("\n");

		for (TextureValidationResult.TextureError error : textureResult.errors()) {
			sb.append("  [").append(error.type()).append("] ").append(error.texturePath()).append("\n");
			sb.append("          ").append(error.message()).append("\n\n");
		}
	}

	private void formatPaletteErrors(StringBuilder sb, PaletteConformityResult paletteResult) {
		sb.append("PALETTE CONFORMITY ERRORS\n");
		sb.append(SECTION_SEPARATOR).append("\n");
		sb.append(String.format("  Total palettes: %d | Valid: %d | Errors: %d | Warnings: %d\n\n",
				paletteResult.totalPalettesValidated(),
				paletteResult.validPalettes(),
				paletteResult.errorCount(),
				paletteResult.warningCount()));

		for (PaletteConformityResult.PaletteError error : paletteResult.errors()) {
			sb.append("  [").append(error.type()).append("] ").append(error.palettePath().getFileName()).append("\n");
			sb.append("          Material: ").append(error.materialName()).append("\n");
			sb.append("          ").append(error.details()).append("\n");
			if (error.actualValue() >= 0 && error.expectedValue() >= 0) {
				sb.append("          Actual: ").append(error.actualValue())
						.append(", Expected: ").append(error.expectedValue()).append("\n");
			}
			sb.append("\n");
		}
	}

	private void formatAnimatedTextureErrors(StringBuilder sb, AnimatedTextureResult animatedResult) {
		sb.append("ANIMATED TEXTURE ERRORS\n");
		sb.append(SECTION_SEPARATOR).append("\n");
		sb.append(String.format("  Animated textures: %d | Valid: %d | Errors: %d\n\n",
				animatedResult.totalAnimatedTextures(),
				animatedResult.validAnimatedTextures(),
				animatedResult.errorCount()));

		for (AnimatedTextureResult.AnimatedTextureError error : animatedResult.errors()) {
			sb.append("  [").append(error.type()).append("] ").append(error.textureName()).append("\n");
			sb.append("          Path: ").append(error.texturePath()).append("\n");
			sb.append("          ").append(error.details()).append("\n");
			if (error.mcmetaPath() != null) {
				sb.append("          mcmeta: ").append(error.mcmetaPath()).append("\n");
			}
			sb.append("\n");
		}
	}

	private void formatWarnings(StringBuilder sb, ValidationResult result) {
		sb.append("WARNINGS\n");
		sb.append(SECTION_SEPARATOR).append("\n");

		// Template warnings
		if (result.templateResult() != null) {
			for (TemplateExpansionResult.TemplateWarning warning : result.templateResult().warnings()) {
				sb.append("  [TEMPLATE] ").append(warning.template()).append("\n");
				sb.append("             Slot: ").append(warning.slotName()).append("\n");
				sb.append("             ").append(warning.message()).append("\n\n");
			}
		}

		// Definition warnings
		if (result.definitionResult() != null) {
			for (DefinitionValidationResult.DefinitionWarning warning : result.definitionResult().warnings()) {
				sb.append("  [DEFINITION] ").append(warning.source()).append("\n");
				sb.append("               ").append(warning.message()).append("\n\n");
			}
		}

		// Model warnings
		if (result.modelResult() != null) {
			for (ModelValidationResult.ModelWarning warning : result.modelResult().warnings()) {
				sb.append("  [MODEL] ").append(warning.modelId()).append("\n");
				sb.append("          ").append(warning.message()).append("\n\n");
			}
		}

		// Texture warnings
		if (result.textureResult() != null) {
			for (TextureValidationResult.TextureWarning warning : result.textureResult().warnings()) {
				sb.append("  [TEXTURE] ").append(warning.texturePath()).append("\n");
				sb.append("            ").append(warning.message()).append("\n\n");
			}
		}

		// Palette warnings
		if (result.paletteResult() != null) {
			for (PaletteConformityResult.PaletteWarning warning : result.paletteResult().warnings()) {
				sb.append("  [PALETTE] ").append(warning.palettePath().getFileName()).append("\n");
				sb.append("            Material: ").append(warning.materialName()).append("\n");
				sb.append("            [").append(warning.type()).append("] ").append(warning.details()).append("\n\n");
			}
		}

		// Animated texture warnings
		if (result.animatedTextureResult() != null) {
			for (AnimatedTextureResult.AnimatedTextureWarning warning : result.animatedTextureResult().warnings()) {
				sb.append("  [ANIMATED] ").append(warning.textureName()).append("\n");
				sb.append("             [").append(warning.type()).append("] ").append(warning.details()).append("\n\n");
			}
		}
	}

	/**
	 * Creates a short single-line summary suitable for build output.
	 *
	 * @param result The validation result
	 * @return Short summary string
	 */
	public String formatShortSummary(ValidationResult result) {
		if (result.hasErrors()) {
			return String.format("VALIDATION FAILED: %d error(s), %d warning(s) | %d definitions, %d templates, %d components",
					result.errorCount(),
					result.warningCount(),
					result.statistics().definitionsLoaded(),
					result.statistics().templatesProcessed(),
					result.statistics().componentsGenerated());
		} else {
			return String.format("VALIDATION PASSED: %d warning(s) | %d definitions, %d templates, %d components",
					result.warningCount(),
					result.statistics().definitionsLoaded(),
					result.statistics().templatesProcessed(),
					result.statistics().componentsGenerated());
		}
	}
}
