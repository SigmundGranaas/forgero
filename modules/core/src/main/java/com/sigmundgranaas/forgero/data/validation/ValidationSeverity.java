package com.sigmundgranaas.forgero.data.validation;

/**
 * Severity levels for validation issues.
 */
public enum ValidationSeverity {
	/**
	 * Critical errors that prevent the component from functioning correctly.
	 * These should cause the component to fail to load in strict mode.
	 */
	ERROR,

	/**
	 * Warnings about potential issues that don't prevent functionality.
	 * Examples: unused attributes, suboptimal configurations.
	 */
	WARNING,

	/**
	 * Informational notes about the validation.
	 * Examples: deprecation notices, optimization suggestions.
	 */
	INFO
}
