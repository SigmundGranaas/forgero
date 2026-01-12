package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.core.component.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates multiple validators to validate components.
 * Aggregates results from all validators and provides a unified report.
 */
public class ComponentValidationEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentValidationEngine.class);

	private final List<ComponentValidator> validators;
	private final ValidationContext context;

	/**
	 * Creates a validation engine with the given validators and context.
	 */
	public ComponentValidationEngine(List<ComponentValidator> validators, ValidationContext context) {
		this.validators = new ArrayList<>(validators);
		this.context = context;
	}

	/**
	 * Validates a single component with all registered validators.
	 *
	 * @param component The component to validate.
	 * @return Aggregated validation result from all validators.
	 */
	public ValidationResult validate(Component component) {
		ValidationResult.Builder builder = new ValidationResult.Builder();

		for (ComponentValidator validator : validators) {
			try {
				ValidationResult result = validator.validate(component, context);
				builder.merge(result);
			} catch (Exception e) {
				LOGGER.error("Validator {} threw exception while validating {}: {}",
						validator.name(), component.id(), e.getMessage());
				builder.add(ValidationIssue.error(
						component.id(),
						"Validator " + validator.name() + " failed: " + e.getMessage()
				));
			}
		}

		return builder.build();
	}

	/**
	 * Validates multiple components with all registered validators.
	 *
	 * @param components The components to validate.
	 * @return Aggregated validation result from all components and validators.
	 */
	public ValidationResult validateAll(List<Component> components) {
		LOGGER.info("Running validation on {} components with {} validators...",
				components.size(), validators.size());

		long startTime = System.currentTimeMillis();
		ValidationResult.Builder builder = new ValidationResult.Builder();

		for (Component component : components) {
			ValidationResult result = validate(component);
			builder.merge(result);
		}

		ValidationResult finalResult = builder.build();
		long endTime = System.currentTimeMillis();

		LOGGER.info("Validation complete in {}ms: {} errors, {} warnings",
				endTime - startTime, finalResult.errorCount(), finalResult.warningCount());

		return finalResult;
	}

	/**
	 * @return The validation context.
	 */
	public ValidationContext context() {
		return context;
	}

	/**
	 * @return The list of validators.
	 */
	public List<ComponentValidator> validators() {
		return List.copyOf(validators);
	}

	/**
	 * Builder for creating a ComponentValidationEngine with default validators.
	 */
	public static class Builder {
		private final List<ComponentValidator> validators = new ArrayList<>();
		private ValidationContext context;

		/**
		 * Adds a validator to the engine.
		 */
		public Builder addValidator(ComponentValidator validator) {
			validators.add(validator);
			return this;
		}

		/**
		 * Adds multiple validators to the engine.
		 */
		public Builder addValidators(List<ComponentValidator> validators) {
			this.validators.addAll(validators);
			return this;
		}

		/**
		 * Sets the validation context.
		 */
		public Builder context(ValidationContext context) {
			this.context = context;
			return this;
		}

		/**
		 * Builds the validation engine.
		 */
		public ComponentValidationEngine build() {
			if (context == null) {
				throw new IllegalStateException("ValidationContext is required");
			}
			return new ComponentValidationEngine(validators, context);
		}
	}
}
