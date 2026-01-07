package com.sigmundgranaas.forgero.model.validation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;

/**
 * Result of validating references within model files.
 * <p>
 * This includes validation of:
 * <ul>
 *   <li>Texture references (do referenced textures exist?)</li>
 *   <li>Parent model references (do parent models exist?)</li>
 *   <li>Slot references (are renderer types valid?)</li>
 * </ul>
 */
public record ReferenceValidationResult(
		List<TextureReferenceError> textureErrors,
		List<ParentReferenceError> parentErrors,
		List<SlotReferenceError> slotErrors,
		List<ReferenceWarning> warnings,
		int totalReferencesChecked
) {
	
	public boolean hasErrors() {
		return !textureErrors.isEmpty() || !parentErrors.isEmpty() || !slotErrors.isEmpty();
	}
	
	public int errorCount() {
		return textureErrors.size() + parentErrors.size() + slotErrors.size();
	}
	
	public int warningCount() {
		return warnings.size();
	}
	
	/**
	 * Error indicating a referenced texture does not exist.
	 */
	public record TextureReferenceError(
			OpenIdentifier modelId,
			String referencedTexture,
			String fieldPath,
			String expectedPath
	) {}
	
	/**
	 * Error indicating a referenced parent model does not exist.
	 */
	public record ParentReferenceError(
			OpenIdentifier modelId,
			String referencedParent,
			String expectedPath
	) {}
	
	/**
	 * Error indicating an invalid slot configuration.
	 */
	public record SlotReferenceError(
			OpenIdentifier modelId,
			String slotId,
			String rendererType,
			String issue
	) {}
	
	/**
	 * Warning about a reference that may be problematic but isn't necessarily an error.
	 */
	public record ReferenceWarning(
			OpenIdentifier modelId,
			String field,
			String message
	) {}
	
	public static ReferenceValidationResult empty() {
		return new ReferenceValidationResult(List.of(), List.of(), List.of(), List.of(), 0);
	}
	
	/**
	 * Merges two validation results.
	 */
	public ReferenceValidationResult merge(ReferenceValidationResult other) {
		return new ReferenceValidationResult(
				concat(textureErrors, other.textureErrors),
				concat(parentErrors, other.parentErrors),
				concat(slotErrors, other.slotErrors),
				concat(warnings, other.warnings),
				totalReferencesChecked + other.totalReferencesChecked
		);
	}
	
	private static <T> List<T> concat(List<T> a, List<T> b) {
		if (a.isEmpty()) return b;
		if (b.isEmpty()) return a;
		return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
	}
}
