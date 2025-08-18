package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * An implementation of {@link AttributeBakingStrategy} that specifically handles
 * {@link CompositeAttributeComponent}s for structured items. It processes the component tree
 * and attempts to form {@link CompositeAttribute}s from groups of {@link CompositeAttributeComponent}s.
 *
 * <p><b>Core Design Principle: Local Composition</b></p>
 * This strategy intentionally enforces a "local composition" model. A {@link CompositeAttribute}
 * can only be formed from a group of {@link CompositeAttributeComponent}s that all originate
 - * from the <strong>same parent {@link Component}</strong> in the tree (e.g., all from a single pickaxe head).
 - * These components must also share the same {@code type} and {@code compositeKey}.
 *
 * <p><b>Discard on Failure</b></p>
 * If a local group of {@link CompositeAttributeComponent}s fails to meet the criteria for forming a valid
 * {@link CompositeAttribute} (e.g., having fewer than two distinct operators), the entire group of components
 * is <strong>discarded</strong>. They do NOT fall back to being treated as simple attributes.
 *
 * <p><b>Design Rationale</b></p>
 * This "local-only" approach ensures that attribute calculations are highly predictable and encapsulated.
 * For example, a "Reinforced Iron Ingot" part will always contribute the same final composite attribute value,
 * regardless of what other parts (like a handle or binding) it is combined with. This prevents complex,
 * hard-to-debug cross-component interactions at the composite level and promotes modular part design.
 * Standard cross-component interactions should be handled using {@link com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute}.
 */
public class CompositeAttributeBakingStrategy implements AttributeBakingStrategy {

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		Component root = componentList.isEmpty() ? null : componentList.get(0);

		List<Attribute> bakedAttributes = new ArrayList<>();

		for (Component currentComponent : componentList) {
			List<CompositeAttributeComponent> compositeComponentsFromCurrent = new ArrayList<>();
			List<Attribute> simpleAttributesFromCurrent = new ArrayList<>();

			// First, filter attributes from the current component based on static conditions
			// and separate them into simple attributes and composite components.
			currentComponent.properties(KEY)
					.stream()
					.filter(attribute -> {
						ResolutionContext resCtx = new ResolutionContext(currentComponent, root);
						return attribute.condition()
								.map(Condition::staticConditions)
								.map(resCtx::test)
								.orElse(true); // If no static conditions, it passes.
					})
					.forEach(attribute -> {
						if (attribute instanceof CompositeAttributeComponent compAttribute) {
							compositeComponentsFromCurrent.add(compAttribute);
						} else {
							simpleAttributesFromCurrent.add(attribute);
						}
					});

			// Add all valid simple attributes from this component directly.
			bakedAttributes.addAll(simpleAttributesFromCurrent);

			// Now, attempt to form composite attributes *from the components gathered from THIS component only*.
			// Group them by their compositeKey (and type, though type should already be consistent per attribute kind).
			Map<String, List<CompositeAttributeComponent>> groupedLocalComposites = compositeComponentsFromCurrent.stream()
					.collect(Collectors.groupingBy(comp -> comp.type().toString() + ":" + comp.compositeKey().toString()));

			for (List<CompositeAttributeComponent> group : groupedLocalComposites.values()) {
				if (!group.isEmpty()) {
					// All components in a group share the same type and compositeKey by design.
					// Try to form a CompositeAttribute.
					CompositeAttribute.of(group.get(0).type(), group.get(0).compositeKey(), group)
							.ifPresentOrElse(
									bakedAttributes::add, // Successfully formed a CompositeAttribute, add it.
									// If CompositeAttribute could not be formed, DO NOTHING (discard).
									() -> { /* Discard these components */ }
							);
				}
			}
		}
		return bakedAttributes;
	}
}
