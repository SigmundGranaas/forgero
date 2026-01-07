package com.sigmundgranaas.forgero.model.validation.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of validating animated textures and their .mcmeta files.
 * <p>
 * Validates:
 * <ul>
 *   <li>Animated textures have corresponding .mcmeta files</li>
 *   <li>.mcmeta files are valid JSON</li>
 *   <li>Animation configuration is valid (frametime, frames, etc.)</li>
 *   <li>Frame count matches texture dimensions</li>
 * </ul>
 */
public record AnimatedTextureResult(
		List<AnimatedTextureError> errors,
		List<AnimatedTextureWarning> warnings,
		int totalAnimatedTextures,
		int validAnimatedTextures,
		int orphanedMcmetaFiles
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
	 * Error types for animated texture validation.
	 */
	public enum ErrorType {
		/** Animated texture (height > width or height > 1 for palette) without .mcmeta */
		MISSING_MCMETA,
		/** .mcmeta file is not valid JSON */
		INVALID_MCMETA_JSON,
		/** .mcmeta missing required "animation" key */
		MISSING_ANIMATION_KEY,
		/** frametime is not a positive integer */
		INVALID_FRAMETIME,
		/** Texture height not divisible by frame height */
		FRAME_COUNT_MISMATCH,
		/** frames array references invalid frame index */
		INVALID_FRAME_INDEX,
		/** Could not read .mcmeta file */
		MCMETA_UNREADABLE
	}
	
	/**
	 * Warning types for animated texture validation.
	 */
	public enum WarningType {
		/** .mcmeta exists but texture doesn't appear to be animated */
		ORPHANED_MCMETA,
		/** Very long animation (many frames) may impact performance */
		LONG_ANIMATION,
		/** frametime is very high (slow animation) */
		SLOW_ANIMATION,
		/** Interpolation enabled with few frames */
		INTERPOLATION_FEW_FRAMES
	}
	
	/**
	 * Represents an error found during animated texture validation.
	 */
	public record AnimatedTextureError(
			Path texturePath,
			Path mcmetaPath,
			String textureName,
			ErrorType type,
			String details
	) {
		public AnimatedTextureError(Path texturePath, String textureName, ErrorType type, String details) {
			this(texturePath, null, textureName, type, details);
		}
	}
	
	/**
	 * Represents a warning found during animated texture validation.
	 */
	public record AnimatedTextureWarning(
			Path texturePath,
			String textureName,
			WarningType type,
			String details
	) {}
	
	public static AnimatedTextureResult empty() {
		return new AnimatedTextureResult(List.of(), List.of(), 0, 0, 0);
	}
	
	/**
	 * Merges two validation results.
	 */
	public AnimatedTextureResult merge(AnimatedTextureResult other) {
		return new AnimatedTextureResult(
				concat(errors, other.errors),
				concat(warnings, other.warnings),
				totalAnimatedTextures + other.totalAnimatedTextures,
				validAnimatedTextures + other.validAnimatedTextures,
				orphanedMcmetaFiles + other.orphanedMcmetaFiles
		);
	}
	
	private static <T> List<T> concat(List<T> a, List<T> b) {
		if (a.isEmpty()) return b;
		if (b.isEmpty()) return a;
		return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
	}
}
