package com.sigmundgranaas.forgero.model.validation.api;

import java.nio.file.Path;
import java.util.List;

public record TextureValidationResult(
		List<TextureError> errors,
		List<TextureWarning> warnings,
		int templatesValidated,
		int palettesValidated
) {
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public record TextureError(
			Path texturePath,
			String message,
			ErrorType type
	) {
	}

	public record TextureWarning(
			Path texturePath,
			String message
	) {
	}

	public enum ErrorType {
		FILE_NOT_FOUND,
		INVALID_PNG,
		NOT_GRAYSCALE,
		PALETTE_TOO_NARROW,
		PALETTE_INVALID_HEIGHT
	}

	public static TextureValidationResult empty() {
		return new TextureValidationResult(List.of(), List.of(), 0, 0);
	}
}
