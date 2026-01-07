package com.sigmundgranaas.forgero.model.validation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;

/**
 * Result of semantic validation of model structure.
 * <p>
 * Validates:
 * <ul>
 *   <li>Layer ordering (unique, positive, reasonable gaps)</li>
 *   <li>Predicate configuration (valid types, required fields)</li>
 *   <li>Mount point configuration (unique names, valid positions)</li>
 * </ul>
 */
public record SemanticValidationResult(
		List<SemanticError> errors,
		List<SemanticWarning> warnings,
		int modelsValidated
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
	
	public enum ErrorType {
		/** Predicate type is unknown */
		UNKNOWN_PREDICATE_TYPE,
		/** Required field missing from predicate */
		MISSING_PREDICATE_FIELD,
		/** Invalid predicate value */
		INVALID_PREDICATE_VALUE,
		/** Mount point name is duplicated */
		DUPLICATE_MOUNT_POINT,
		/** Layer order is negative */
		NEGATIVE_LAYER_ORDER
	}
	
	public enum WarningType {
		/** Multiple layers have same order */
		DUPLICATE_LAYER_ORDER,
		/** Large gap between consecutive layer orders */
		LAYER_ORDER_GAP,
		/** Mount point position may be outside texture bounds */
		MOUNT_POINT_POSITION,
		/** Slot ID duplicated */
		DUPLICATE_SLOT_ID
	}
	
	public record SemanticError(
			OpenIdentifier modelId,
			ErrorType type,
			String field,
			String details
	) {}
	
	public record SemanticWarning(
			OpenIdentifier modelId,
			WarningType type,
			String field,
			String details
	) {}
	
	public static SemanticValidationResult empty() {
		return new SemanticValidationResult(List.of(), List.of(), 0);
	}
	
	public SemanticValidationResult merge(SemanticValidationResult other) {
		return new SemanticValidationResult(
				concat(errors, other.errors),
				concat(warnings, other.warnings),
				modelsValidated + other.modelsValidated
		);
	}
	
	private static <T> List<T> concat(List<T> a, List<T> b) {
		if (a.isEmpty()) return b;
		if (b.isEmpty()) return a;
		return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
	}
}
