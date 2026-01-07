package com.sigmundgranaas.forgero.model.validation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;

public record ModelValidationResult(
		List<ModelError> errors,
		List<ModelWarning> warnings,
		int totalModelsValidated
) {
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public record ModelError(
			OpenIdentifier modelId,
			String message,
			ErrorType type
	) {
	}

	public record ModelWarning(
			OpenIdentifier modelId,
			String message
	) {
	}

	public enum ErrorType {
		INVALID_JSON,
		MISSING_TEXTURE_REFERENCE,
		MISSING_SLOT_REFERENCE,
		UNKNOWN_PREDICATE_TYPE,
		INVALID_LAYER_ORDER
	}

	public static ModelValidationResult empty() {
		return new ModelValidationResult(List.of(), List.of(), 0);
	}
}
