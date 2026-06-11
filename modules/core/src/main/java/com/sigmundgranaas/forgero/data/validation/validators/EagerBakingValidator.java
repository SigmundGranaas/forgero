package com.sigmundgranaas.forgero.data.validation.validators;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.data.validation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates fully-structured components (equipment) by running the baking process at creation time.
 * <p>
 * This validator only runs on components that have a complete component tree (i.e., equipment with
 * all required parts). Parts and materials are NOT validated by this because their context is
 * unknown until they're composed into a tool.
 * <p>
 * The validator performs:
 * <ul>
 *   <li>Runs attribute baking to detect composition failures</li>
 *   <li>Reports attributes that were discarded due to missing pairs</li>
 *   <li>Identifies potential configuration issues</li>
 * </ul>
 */
public class EagerBakingValidator implements ComponentValidator {

	private final AttributeEngine attributeEngine = new AttributeEngine();

	@Override
	public ValidationResult validate(Component component, ValidationContext context) {
		// Only validate fully-structured components (equipment)
		if (!isFullyStructured(component)) {
			return ValidationResult.empty();
		}

		ValidationResult.Builder builder = new ValidationResult.Builder();

		try {
			validateAttributeComposition(component, builder);
		} catch (Exception e) {
			builder.add(ValidationIssue.error(
					component.id(),
					"Attribute baking failed with exception: " + e.getMessage()
			));
		}

		return builder.build();
	}

	/**
	 * Determines if a component is "fully structured" - meaning it has a complete
	 * component tree with all required parts.
	 * <p>
	 * Equipment like tools and weapons are fully structured.
	 * Parts like pickaxe_head or handle are NOT fully structured because
	 * they need to be composed into equipment to have full context.
	 */
	private boolean isFullyStructured(Component component) {
		if (!(component instanceof StructuredComponent structured)) {
			return false;
		}

		// Check if this has structured children (equipment has parts as children)
		// If any child is also structured, this is likely equipment
		for (ComponentPart part : structured.structure().allParts()) {
			if (part.getContent() instanceof StructuredComponent) {
				return true;
			}
		}

		// A simple structured component with only static children is considered
		// partially structured (like a part with material + shape)
		return false;
	}

	/**
	 * Runs the baking process and analyzes the results for potential issues.
	 */
	private void validateAttributeComposition(Component component, ValidationResult.Builder builder) {
		// Collect all components in the tree (pre-order traversal)
		List<Component> componentList = traverse(component);

		// Collect all attributes BEFORE baking to compare with results
		Map<OpenIdentifier, List<AttributeInfo>> attributesBefore = collectAllAttributes(componentList);

		// Run baking
		BakedAttributes bakedAttributes = attributeEngine.compile(componentList.stream());

		// Collect attribute types that made it through baking
		Set<OpenIdentifier> bakedTypes = bakedAttributes.byType().keySet();

		// Analyze what was discarded
		analyzeDiscardedAttributes(component, attributesBefore, bakedTypes, builder);
	}

	/**
	 * Pre-order traversal of the component tree.
	 */
	private List<Component> traverse(Component root) {
		List<Component> result = new ArrayList<>();
		traverseRecursive(root, result);
		return result;
	}

	private void traverseRecursive(Component component, List<Component> result) {
		result.add(component);
		for (Component child : component.getChildren()) {
			traverseRecursive(child, result);
		}
	}

	/**
	 * Collects all attributes from all components, grouped by type.
	 */
	private Map<OpenIdentifier, List<AttributeInfo>> collectAllAttributes(List<Component> components) {
		Map<OpenIdentifier, List<AttributeInfo>> result = new HashMap<>();

		for (Component comp : components) {
			for (Attribute attr : comp.properties(Attribute.KEY)) {
				AttributeInfo info = new AttributeInfo(
						comp.id(),
						attr.type(),
						attr.value(),
						attr.operator().order(),
						attr.scope().orElse(null)
				);
				result.computeIfAbsent(attr.type(), k -> new ArrayList<>()).add(info);
			}
		}

		return result;
	}

	/**
	 * Analyzes which attributes were discarded during baking and why.
	 */
	private void analyzeDiscardedAttributes(
			Component root,
			Map<OpenIdentifier, List<AttributeInfo>> attributesBefore,
			Set<OpenIdentifier> bakedTypes,
			ValidationResult.Builder builder
	) {
		for (Map.Entry<OpenIdentifier, List<AttributeInfo>> entry : attributesBefore.entrySet()) {
			OpenIdentifier attrType = entry.getKey();
			List<AttributeInfo> infos = entry.getValue();

			// Check if any part-composite attributes were discarded
			List<AttributeInfo> partCompositeAttrs = infos.stream()
					.filter(info -> AttributeScope.PART_COMPOSITE.equals(info.scope))
					.toList();

			if (!partCompositeAttrs.isEmpty() && !bakedTypes.contains(attrType)) {
				// This attribute type was defined with part-composite context but didn't make it through
				analyzePartCompositeFailure(root, attrType, partCompositeAttrs, builder);
			}
		}
	}

	/**
	 * Analyzes why a part-composite attribute failed to compose.
	 */
	private void analyzePartCompositeFailure(
			Component root,
			OpenIdentifier attrType,
			List<AttributeInfo> attrs,
			ValidationResult.Builder builder
	) {
		// Check for base (order 1) and multiplier (order 2) presence
		boolean hasBase = attrs.stream().anyMatch(a -> a.operatorOrder == 1);
		boolean hasMultiplier = attrs.stream().anyMatch(a -> a.operatorOrder == 2);

		// Check if they come from different sources
		Set<OpenIdentifier> baseSources = attrs.stream()
				.filter(a -> a.operatorOrder == 1)
				.map(a -> a.sourceId)
				.collect(Collectors.toSet());

		Set<OpenIdentifier> multiplierSources = attrs.stream()
				.filter(a -> a.operatorOrder == 2)
				.map(a -> a.sourceId)
				.collect(Collectors.toSet());

		boolean fromDifferentSources = !baseSources.equals(multiplierSources);

		if (!hasBase && hasMultiplier) {
			// Has multiplier but no base value
			String sources = multiplierSources.stream()
					.map(OpenIdentifier::toString)
					.collect(Collectors.joining(", "));

			builder.add(ValidationIssue.warning(
					root.id(),
					"Attribute '" + attrType + "' has multiplier(s) from [" + sources + "] but no base value",
					"composition." + attrType,
					"Add a base value (addition operator) for this attribute type to a material"
			));
		} else if (hasBase && !hasMultiplier) {
			// Has base value but no multiplier
			String sources = baseSources.stream()
					.map(OpenIdentifier::toString)
					.collect(Collectors.joining(", "));

			builder.add(ValidationIssue.warning(
					root.id(),
					"Attribute '" + attrType + "' has base value(s) from [" + sources + "] but no multiplier",
					"composition." + attrType,
					"Add a multiplier (multiplication operator) for this attribute type to a shape"
			));
		} else if (hasBase && hasMultiplier && !fromDifferentSources) {
			// Both present but from same source - won't compose
			String sources = baseSources.stream()
					.map(OpenIdentifier::toString)
					.collect(Collectors.joining(", "));

			builder.add(ValidationIssue.warning(
					root.id(),
					"Attribute '" + attrType + "' has both base and multiplier from same source(s) [" + sources + "]",
					"composition." + attrType,
					"Part-composite requires base and multiplier from different components"
			));
		}
	}

	@Override
	public String name() {
		return "EagerBakingValidator";
	}

	/**
	 * Info about an attribute for analysis.
	 */
	private record AttributeInfo(
			OpenIdentifier sourceId,
			OpenIdentifier type,
			float value,
			int operatorOrder,
			OpenIdentifier scope
	) {
	}
}
