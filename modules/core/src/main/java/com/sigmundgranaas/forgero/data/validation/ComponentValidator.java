package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.core.component.api.Component;

/**
 * Interface for component validators.
 * Each validator checks a specific aspect of component validity.
 */
@FunctionalInterface
public interface ComponentValidator {

	/**
	 * Validates the given component and returns any issues found.
	 *
	 * @param component The component to validate.
	 * @param context   The validation context providing access to registries and other components.
	 * @return A validation result containing any issues found.
	 */
	ValidationResult validate(Component component, ValidationContext context);

	/**
	 * @return A human-readable name for this validator (for logging).
	 */
	default String name() {
		return getClass().getSimpleName();
	}
}
