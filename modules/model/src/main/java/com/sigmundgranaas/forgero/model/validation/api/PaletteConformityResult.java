package com.sigmundgranaas.forgero.model.validation.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Result of validating palette conformity.
 * <p>
 * Palettes must follow strict rules:
 * <ul>
 *   <li>Height must be exactly 1 pixel (for non-animated)</li>
 *   <li>Width must be at least 8 pixels</li>
 *   <li>No transparent pixels</li>
 *   <li>Colors should progress dark to light (left to right)</li>
 * </ul>
 */
public record PaletteConformityResult(
		List<PaletteError> errors,
		List<PaletteWarning> warnings,
		int totalPalettesValidated,
		int validPalettes,
		Map<Integer, Integer> widthDistribution
) {
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public int errorCount() {
		return errors.size();
	}
	
	public int warningCount() {
		return warnings.size();
	}
	
	/**
	 * Error types for palette conformity validation.
	 */
	public enum ErrorType {
		/** Palette height is not 1 pixel */
		INVALID_HEIGHT,
		/** Palette width is less than minimum (8 pixels) */
		TOO_NARROW,
		/** Palette contains pixels with alpha < 255 */
		HAS_TRANSPARENCY,
		/** Could not read the palette file */
		UNREADABLE,
		/** File is not a valid PNG */
		INVALID_FORMAT
	}
	
	/**
	 * Warning types for palette conformity validation.
	 */
	public enum WarningType {
		/** Width is not a standard size (8, 16, 32) */
		NON_STANDARD_WIDTH,
		/** Width differs from most common width in pack */
		INCONSISTENT_WIDTH,
		/** Colors progress light to dark instead of dark to light */
		INVERTED_PROGRESSION,
		/** Palette has very low contrast */
		LOW_CONTRAST
	}
	
	/**
	 * Represents an error found during palette validation.
	 */
	public record PaletteError(
			Path palettePath,
			String materialName,
			ErrorType type,
			String details,
			int actualValue,
			int expectedValue
	) {
		public PaletteError(Path palettePath, String materialName, ErrorType type, String details) {
			this(palettePath, materialName, type, details, -1, -1);
		}
	}
	
	/**
	 * Represents a warning found during palette validation.
	 */
	public record PaletteWarning(
			Path palettePath,
			String materialName,
			WarningType type,
			String details
	) {}
	
	public static PaletteConformityResult empty() {
		return new PaletteConformityResult(List.of(), List.of(), 0, 0, Map.of());
	}
	
	/**
	 * Merges two validation results.
	 */
	public PaletteConformityResult merge(PaletteConformityResult other) {
		var mergedWidths = new java.util.HashMap<>(widthDistribution);
		other.widthDistribution.forEach((k, v) -> mergedWidths.merge(k, v, Integer::sum));
		
		return new PaletteConformityResult(
				concat(errors, other.errors),
				concat(warnings, other.warnings),
				totalPalettesValidated + other.totalPalettesValidated,
				validPalettes + other.validPalettes,
				Map.copyOf(mergedWidths)
		);
	}
	
	private static <T> List<T> concat(List<T> a, List<T> b) {
		if (a.isEmpty()) return b;
		if (b.isEmpty()) return a;
		return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
	}
}
