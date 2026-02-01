package com.sigmundgranaas.forgero.data.validation.validators;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.validation.*;

import java.util.List;
import java.util.Set;

/**
 * Validates the schema/structure of attributes on components.
 * <ul>
 *   <li>Checks that attributes have valid types</li>
 *   <li>Validates operator compatibility with scope</li>
 *   <li>Warns about potentially problematic attribute configurations</li>
 * </ul>
 */
public class AttributeSchemaValidator implements ComponentValidator {

	// Known valid scopes
	private static final Set<OpenIdentifier> KNOWN_SCOPES = Set.of(
			AttributeScope.LOCAL,
			AttributeScope.PART_COMPOSITE,
			AttributeScope.EQUIPMENT_COMPOSITE,
			AttributeScope.UPGRADE
	);

	@Override
	public ValidationResult validate(Component component, ValidationContext context) {
		ValidationResult.Builder builder = new ValidationResult.Builder();

		List<Attribute> attributes = component.properties(Attribute.KEY);
		for (int i = 0; i < attributes.size(); i++) {
			Attribute attr = attributes.get(i);
			String location = "properties.attributes[" + i + "]";

			validateAttributeType(component, attr, location, builder);
			validateOperator(component, attr, location, builder);
			validateScopeCompatibility(component, attr, location, builder);
			validateValue(component, attr, location, builder);
		}

		return builder.build();
	}

	private void validateAttributeType(Component component, Attribute attr, String location, ValidationResult.Builder builder) {
		OpenIdentifier type = attr.type();
		if (type == null) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute has null type",
					location + ".type"
			));
			return;
		}

		if (type.path().isEmpty()) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute has empty type path",
					location + ".type"
			));
		}
	}

	private void validateOperator(Component component, Attribute attr, String location, ValidationResult.Builder builder) {
		Operator operator = attr.operator();
		if (operator == null) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute '" + attr.type() + "' has null operator",
					location + ".operator"
			));
		}
	}

	private void validateScopeCompatibility(Component component, Attribute attr, String location, ValidationResult.Builder builder) {
		attr.scope().ifPresent(scope -> {
			// Warn if using unknown scope
			if (!KNOWN_SCOPES.contains(scope)) {
				builder.add(ValidationIssue.info(
						component.id(),
						"Attribute '" + attr.type() + "' uses unknown scope: " + scope,
						location + ".scope"
				));
			}

			// For part-composite scope, validate composition rules
			if (scope.equals(AttributeScope.PART_COMPOSITE)) {
				validatePartCompositeRules(component, attr, location, builder);
			}
		});
	}

	private void validatePartCompositeRules(Component component, Attribute attr, String location, ValidationResult.Builder builder) {
		Operator operator = attr.operator();
		if (operator == null) return;

		int order = operator.order();

		// Part-composite attributes should typically be:
		// - Base values (order 1: addition/subtraction) from materials
		// - Multipliers (order 2: multiplication/division) from shapes
		// This is just informational, not an error
		if (order != 1 && order != 2) {
			builder.add(ValidationIssue.info(
					component.id(),
					"Attribute '" + attr.type() + "' in part-composite context has operator order " + order +
							" (typical values are 1 for base, 2 for multiplier)",
					location
			));
		}
	}

	private void validateValue(Component component, Attribute attr, String location, ValidationResult.Builder builder) {
		float value = attr.value();

		// Check for NaN or Infinity
		if (Float.isNaN(value)) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute '" + attr.type() + "' has NaN value",
					location + ".value"
			));
		}

		if (Float.isInfinite(value)) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute '" + attr.type() + "' has infinite value",
					location + ".value"
			));
		}

		// Warn about zero multipliers
		Operator operator = attr.operator();
		if (operator != null && operator.order() == 2 && value == 0) {
			builder.add(ValidationIssue.warning(
					component.id(),
					"Attribute '" + attr.type() + "' has zero multiplier which will zero out all values",
					location + ".value"
			));
		}
	}

	@Override
	public String name() {
		return "AttributeSchemaValidator";
	}
}
