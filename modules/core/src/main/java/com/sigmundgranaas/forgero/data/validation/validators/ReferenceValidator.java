package com.sigmundgranaas.forgero.data.validation.validators;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.validation.*;

import java.util.List;

/**
 * Validates that all references in a component are resolvable.
 * <ul>
 *   <li>Tags referenced in conditions exist in the tag graph</li>
 *   <li>Slot types referenced in conditions are valid</li>
 *   <li>Attribute contexts are valid identifiers</li>
 * </ul>
 */
public class ReferenceValidator implements ComponentValidator {

	@Override
	public ValidationResult validate(Component component, ValidationContext context) {
		ValidationResult.Builder builder = new ValidationResult.Builder();

		// Validate tags on the component itself exist
		for (OpenIdentifier tag : component.getTags()) {
			if (!context.tagExists(tag)) {
				builder.add(ValidationIssue.warning(
						component.id(),
						"Component has tag '" + tag + "' which does not exist in the tag graph",
						"tags"
				));
			}
		}

		// Validate attributes and their conditions
		List<Attribute> attributes = component.properties(Attribute.KEY);
		for (int i = 0; i < attributes.size(); i++) {
			Attribute attr = attributes.get(i);
			String location = "properties.attributes[" + i + "]";

			// Validate scope references
			attr.scope().ifPresent(scope -> {
				// Scope identifiers don't need to exist in tag graph,
				// but we can warn if they look malformed
				if (scope.path().isEmpty()) {
					builder.add(ValidationIssue.warning(
							component.id(),
							"Attribute '" + attr.type() + "' has empty scope identifier",
							location + ".scope"
					));
				}
			});

			// Validate condition references
			attr.condition().ifPresent(condition -> {
				validateConditionReferences(component, condition, location + ".condition", context, builder);
			});
		}

		return builder.build();
	}

	private void validateConditionReferences(
			Component component,
			Condition condition,
			String location,
			ValidationContext context,
			ValidationResult.Builder builder
	) {
		for (StaticCondition staticCond : condition.staticConditions()) {
			validateStaticCondition(component, staticCond, location, context, builder);
		}
		// Dynamic conditions are runtime-only, no static references to validate here
	}

	private void validateStaticCondition(
			Component component,
			StaticCondition condition,
			String location,
			ValidationContext context,
			ValidationResult.Builder builder
	) {
		// Check InSlotTypeCondition references
		if (condition instanceof InSlotTypeCondition slotCond) {
			OpenIdentifier slotType = slotCond.slotType();
			// Slot types should ideally be known, but this is a soft check
			// as slot types are often defined dynamically
			if (slotType.path().isEmpty()) {
				builder.add(ValidationIssue.warning(
						component.id(),
						"InSlotTypeCondition references empty slot type",
						location
				));
			}
		}

		// Check TagMatchCondition references
		if (condition instanceof TagMatchCondition tagCond) {
			OpenIdentifier tag = tagCond.tag();
			if (tag != null && !context.tagExists(tag)) {
				builder.add(ValidationIssue.warning(
						component.id(),
						"Condition references tag '" + tag + "' which does not exist in the tag graph",
						location,
						"Ensure the tag is defined in your tag files, or this condition will never match"
				));
			}
		}
	}

	@Override
	public String name() {
		return "ReferenceValidator";
	}
}
