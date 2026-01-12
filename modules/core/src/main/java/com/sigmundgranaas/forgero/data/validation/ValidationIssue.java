package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * Represents a single validation issue (error, warning, or info) found during component validation.
 *
 * @param componentId The ID of the component where the issue was found.
 * @param message     Human-readable description of the issue.
 * @param severity    The severity level of the issue.
 * @param location    Optional path within the component where the issue occurred (e.g., "properties.attributes[2].condition").
 * @param suggestion  Optional suggestion for how to fix the issue.
 */
public record ValidationIssue(
		OpenIdentifier componentId,
		String message,
		ValidationSeverity severity,
		Optional<String> location,
		Optional<String> suggestion
) {
	/**
	 * Creates an error-level issue.
	 */
	public static ValidationIssue error(OpenIdentifier componentId, String message) {
		return new ValidationIssue(componentId, message, ValidationSeverity.ERROR, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates an error-level issue with location context.
	 */
	public static ValidationIssue error(OpenIdentifier componentId, String message, String location) {
		return new ValidationIssue(componentId, message, ValidationSeverity.ERROR, Optional.of(location), Optional.empty());
	}

	/**
	 * Creates an error-level issue with location and suggestion.
	 */
	public static ValidationIssue error(OpenIdentifier componentId, String message, String location, String suggestion) {
		return new ValidationIssue(componentId, message, ValidationSeverity.ERROR, Optional.of(location), Optional.of(suggestion));
	}

	/**
	 * Creates a warning-level issue.
	 */
	public static ValidationIssue warning(OpenIdentifier componentId, String message) {
		return new ValidationIssue(componentId, message, ValidationSeverity.WARNING, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates a warning-level issue with location context.
	 */
	public static ValidationIssue warning(OpenIdentifier componentId, String message, String location) {
		return new ValidationIssue(componentId, message, ValidationSeverity.WARNING, Optional.of(location), Optional.empty());
	}

	/**
	 * Creates a warning-level issue with location and suggestion.
	 */
	public static ValidationIssue warning(OpenIdentifier componentId, String message, String location, String suggestion) {
		return new ValidationIssue(componentId, message, ValidationSeverity.WARNING, Optional.of(location), Optional.of(suggestion));
	}

	/**
	 * Creates an info-level issue.
	 */
	public static ValidationIssue info(OpenIdentifier componentId, String message) {
		return new ValidationIssue(componentId, message, ValidationSeverity.INFO, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates an info-level issue with location.
	 */
	public static ValidationIssue info(OpenIdentifier componentId, String message, String location) {
		return new ValidationIssue(componentId, message, ValidationSeverity.INFO, Optional.of(location), Optional.empty());
	}

	/**
	 * Formats this issue as a human-readable string.
	 */
	public String format() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(severity).append("] ").append(componentId);

		location.ifPresent(loc -> sb.append(" at ").append(loc));
		sb.append("\n    ").append(message);
		suggestion.ifPresent(sug -> sb.append("\n    FIX: ").append(sug));

		return sb.toString();
	}
}
