package com.sigmundgranaas.forgero.data.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an error that occurred during JSON parsing or codec processing.
 */
public record ParsingError(
		/**
		 * The identifier of the resource that failed to parse.
		 */
		OpenIdentifier source,

		/**
		 * Human-readable error message.
		 */
		String message,

		/**
		 * The underlying exception, if any.
		 */
		@Nullable Exception cause
) {
	/**
	 * Creates a parsing error without an underlying exception.
	 */
	public static ParsingError of(OpenIdentifier source, String message) {
		return new ParsingError(source, message, null);
	}

	/**
	 * Creates a parsing error with an underlying exception.
	 */
	public static ParsingError of(OpenIdentifier source, String message, Exception cause) {
		return new ParsingError(source, message, cause);
	}
}
