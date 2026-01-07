package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeContextHandler;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.*;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * A baking strategy that handles context-based attribute composition for structured components.
 *
 * <p><b>Context-Based Composition</b></p>
 * Attributes can declare a {@code context} that determines how they participate in composition:
 * <ul>
 *   <li>{@code forgero:part-composite} - Composed using intersection logic (shape × material)</li>
 *   <li>No context (default) - Passes through without special handling</li>
 * </ul>
 *
 * <p><b>Part Composite Composition</b></p>
 * For StructuredComponents, attributes with {@code part-composite} context are composed:
 * <ul>
 *   <li>Collected from the component itself AND its structure slot children</li>
 *   <li>Grouped by source (e.g., "self" vs slot name)</li>
 *   <li>Intersection rule: only types with base+multiplier from DIFFERENT sources compose</li>
 *   <li>Result: composed attributes with no context (ready for propagation)</li>
 * </ul>
 *
 * <p><b>Example</b></p>
 * A pickaxe_head (StructuredComponent) with material slot:
 * <ul>
 *   <li>Self (shape): mining_speed ×1.2, durability ×1.0</li>
 *   <li>Material (iron): mining_speed +6.0, durability +240, armor +5</li>
 *   <li>Composed: mining_speed=7.2, durability=240 (armor excluded - no shape multiplier)</li>
 * </ul>
 */
public class CompositeAttributeBakingStrategy implements AttributeBakingStrategy {

	private final PartCompositeContextHandler partCompositeHandler = PartCompositeContextHandler.INSTANCE;

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return List.of();
		}
		Component root = componentList.get(0);

		List<Attribute> result = new ArrayList<>();

		// Step 1: Handle context-based composition for the root if it's structured
		if (root instanceof StructuredComponent structured) {
			result.addAll(composePartAttributes(structured));
		}

		// Step 2: Collect default (no context) attributes from all components
		// and filter by static conditions
		for (Component component : componentList) {
			ResolutionContext resCtx = new ResolutionContext(component, root);

			List<Attribute> defaultAttributes = component.properties(KEY).stream()
					// Only default attributes (no context)
					.filter(attr -> attr.context().isEmpty())
					// Filter by static conditions
					.filter(attribute -> attribute.condition()
							.map(Condition::staticConditions)
							.map(resCtx::test)
							.orElse(true))
					.toList();

			result.addAll(defaultAttributes);
		}

		return result;
	}

	/**
	 * Composes part-composite attributes from a StructuredComponent and its structure children.
	 *
	 * @param structured The structured component (e.g., a part with shape+material)
	 * @return Composed attributes with no context
	 */
	private List<Attribute> composePartAttributes(StructuredComponent structured) {
		Map<String, List<Attribute>> sources = new HashMap<>();

		// Collect part-composite attributes from self (shape/template attributes)
		List<Attribute> selfAttrs = structured.properties(KEY).stream()
				.filter(attr -> attr.context()
						.map(ctx -> ctx.equals(AttributeContext.PART_COMPOSITE))
						.orElse(false))
				.toList();

		if (!selfAttrs.isEmpty()) {
			sources.put("self", selfAttrs);
		}

		// Collect attributes from each structure slot child
		// For structured children (like blades), compose their attributes first
		for (ComponentPart part : structured.structure().allParts()) {
			Component child = part.getContent();
			String slotName = part.id().toString();

			// If child is structured, compose it first and use the composed result
			// This handles the hierarchy: tool → part → material
			// Where the part has template multipliers and material has base values
			List<Attribute> childAttrs = collectOrComposeChildAttributes(child);

			if (!childAttrs.isEmpty()) {
				sources.put(slotName, childAttrs);
			}
		}

		// Compose if we have attributes from multiple sources
		if (sources.size() >= 2) {
			// Try intersection composition (shape × material pattern)
			List<Attribute> composed = partCompositeHandler.compose(sources);
			
			// If intersection fails (e.g., equipment with already-composed parts), 
			// fall back to additive composition (sum all values of same type)
			if (composed.isEmpty()) {
				composed = additiveCompose(sources);
			}
			return composed;
		} else if (sources.size() == 1) {
			// Only one source - return those attributes directly
			// This handles: equipment with single part (blade inherits composed values)
			return sources.values().iterator().next();
		}

		return List.of();
	}

	/**
	 * Additive composition: sums all values of the same attribute type across sources.
	 * Used when intersection composition fails (e.g., equipment with already-composed parts).
	 *
	 * Example: pickaxe with head (durability=240) and handle (durability=50) → durability=290
	 */
	private List<Attribute> additiveCompose(Map<String, List<Attribute>> sources) {
		Map<OpenIdentifier, Float> sums = new HashMap<>();
		
		for (List<Attribute> attrs : sources.values()) {
			for (Attribute attr : attrs) {
				sums.merge(attr.type(), attr.value(), Float::sum);
			}
		}
		
		return sums.entrySet().stream()
			.<Attribute>map(e -> SimpleAttribute.resolved(e.getKey(), e.getValue()))
			.toList();
	}

	/**
	 * For structured children: compose their attributes first and return the composed result.
	 * For non-structured children: collect raw part-composite attributes.
	 *
	 * This is the key to supporting the hierarchy: tool → part → material
	 * When a blade contains a material, we compose the blade's attributes first,
	 * so the tool sees already-composed values instead of raw part-composite attrs.
	 */
	private List<Attribute> collectOrComposeChildAttributes(Component child) {
		if (child instanceof StructuredComponent structuredChild) {
			List<Attribute> composed = composePartAttributes(structuredChild);
			if (!composed.isEmpty()) {
				return composed;
			}
		}
		return collectPartCompositeAttributesRecursively(child);
	}

	/**
	 * Recursively collects all part-composite attributes from a component and all its descendants.
	 * This ensures that nested structures (e.g., a part containing a material) have all their
	 * attributes properly included in composition.
	 *
	 * @param component The component to collect from
	 * @return List of all part-composite attributes from this component and its descendants
	 */
	private List<Attribute> collectPartCompositeAttributesRecursively(Component component) {
		List<Attribute> result = new ArrayList<>();

		// Add component's own part-composite attributes
		result.addAll(component.properties(KEY).stream()
				.filter(attr -> attr.context()
						.map(ctx -> ctx.equals(AttributeContext.PART_COMPOSITE))
						.orElse(false))
				.toList());

		// Recursively collect from children if this is a structured component
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				result.addAll(collectPartCompositeAttributesRecursively(part.getContent()));
			}
		}

		return result;
	}
}
