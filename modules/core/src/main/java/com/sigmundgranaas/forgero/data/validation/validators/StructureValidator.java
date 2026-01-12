package com.sigmundgranaas.forgero.data.validation.validators;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.data.validation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates the structural integrity of components.
 * <ul>
 *   <li>Checks for empty structure slots</li>
 *   <li>Detects circular references in component trees</li>
 *   <li>Validates that children are properly constructed</li>
 * </ul>
 */
public class StructureValidator implements ComponentValidator {

	@Override
	public ValidationResult validate(Component component, ValidationContext context) {
		ValidationResult.Builder builder = new ValidationResult.Builder();

		if (component instanceof StructuredComponent structured) {
			validateStructure(structured, builder, new HashSet<>());
		}

		if (component instanceof CustomizableComponent customizable) {
			validateUpgradeSlots(customizable, component, builder);
		}

		return builder.build();
	}

	private void validateStructure(StructuredComponent structured, ValidationResult.Builder builder, Set<Object> visited) {
		// Circular reference detection using identity
		if (!visited.add(System.identityHashCode(structured))) {
			builder.add(ValidationIssue.error(
					structured.id(),
					"Circular reference detected in component structure"
			));
			return;
		}

		for (ComponentPart part : structured.structure().allParts()) {
			Component child = part.getContent();

			if (child == null) {
				builder.add(ValidationIssue.error(
						structured.id(),
						"Structure slot '" + part.id() + "' is empty (null content)",
						"structure." + part.id(),
						"Ensure all structure slots are filled during template expansion"
				));
				continue;
			}

			// Validate child is of expected type
			if (part.partType() != null && !child.getTags().contains(part.partType())) {
				builder.add(ValidationIssue.warning(
						structured.id(),
						"Child in slot '" + part.id() + "' does not have expected tag: " + part.partType(),
						"structure." + part.id()
				));
			}

			// Recursively validate structured children
			if (child instanceof StructuredComponent structuredChild) {
				validateStructure(structuredChild, builder, visited);
			}
		}
	}

	private void validateUpgradeSlots(CustomizableComponent customizable, Component component, ValidationResult.Builder builder) {
		// Check that upgrade slots have valid types
		customizable.upgrades().slots().all().forEach(slot -> {
			if (slot.slotType() == null) {
				builder.add(ValidationIssue.warning(
						component.id(),
						"Upgrade slot '" + slot.id() + "' has no type defined",
						"upgrades." + slot.id()
				));
			}
		});
	}

	@Override
	public String name() {
		return "StructureValidator";
	}
}
