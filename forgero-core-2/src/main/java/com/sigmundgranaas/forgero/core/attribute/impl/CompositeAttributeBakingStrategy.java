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
 * An implementation of {@link AttributeBakingStrategy} that combines {@link CompositeAttributeComponent}s
 * from across all structured parts to form final {@link CompositeAttribute}s.
 *
 * <p><b>Core Logic: Global Combination</b></p>
 * This strategy operates in three main steps:
 * <ol>
 *   <li><b>Gather:</b> It first collects all attributes from every {@link Component} in the input stream,
 *   filtering them based on their static conditions.</li>
 *   <li><b>Partition:</b> It separates the collected attributes into two lists: simple {@link Attribute}s and
 *   {@link CompositeAttributeComponent}s.</li>
 *   <li><b>Combine:</b> It groups the {@link CompositeAttributeComponent}s by their type and composite key.
 *   Each group is then used to attempt to form a single {@link CompositeAttribute}.</li>
 * </ol>
 *
 * <p><b>Grouping</b></p>
 * A {@link CompositeAttribute} is formed from a group of {@link CompositeAttributeComponent}s that share
 * the same {@code type} (e.g., ATTACK_DAMAGE) and {@code compositeKey} (a unique identifier for a composite calculation).
 * This allows, for example, a component from a tool head and a component from a handle to contribute to the
 * same final mining speed attribute.
 *
 * <p><b>Discard on Failure</b></p>
 * If a group of {@link CompositeAttributeComponent}s fails to meet the criteria for forming a valid
 * {@link CompositeAttribute} (e.g., having fewer than two distinct operators), the entire group of components
 * is <strong>discarded</strong>. They do NOT fall back to being treated as simple attributes.
 *
 * <p><b>Design Rationale</b></p>
 * This "global combination" approach is essential for the composite attribute system to work as intended,
 * allowing different parts of a larger structure to synergize and contribute to a final, calculated value.
 * Simple attributes, which are not combined in this way, are passed through directly.
 */
public class CompositeAttributeBakingStrategy implements AttributeBakingStrategy {

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return List.of();
		}
		Component root = componentList.get(0);

		// 1. GATHER: Flatten the stream of components into a single stream of all their valid attributes.
		// This is the key change: we now consider all components together, not one by one.
		List<Attribute> allValidAttributes = componentList.stream()
				.flatMap(component -> {
					ResolutionContext resCtx = new ResolutionContext(component, root);
					return component.properties(KEY).stream()
							.filter(attribute -> attribute.condition()
									.map(Condition::staticConditions)
									.map(resCtx::test)
									.orElse(true)); // If no static conditions, it passes.
				})
				.toList();

		// 2. PARTITION: Separate attributes into simple ones and composite components.
		Map<Boolean, List<Attribute>> partitionedAttributes = allValidAttributes.stream()
				.collect(Collectors.partitioningBy(attr -> attr instanceof CompositeAttributeComponent));

		List<Attribute> simpleAttributes = partitionedAttributes.get(false);
		List<CompositeAttributeComponent> allCompositeComponents = partitionedAttributes.get(true)
				.stream()
				.map(CompositeAttributeComponent.class::cast)
				.toList();

		// The final list of baked attributes starts with all the simple attributes.
		List<Attribute> bakedAttributes = new ArrayList<>(simpleAttributes);

		// 3. COMBINE: Group all composite components by their key and attempt to form CompositeAttributes.
		Map<String, List<CompositeAttributeComponent>> groupedComposites = allCompositeComponents.stream()
				.collect(Collectors.groupingBy(comp -> comp.type().toString() + ":" + comp.compositeKey().toString()));

		for (List<CompositeAttributeComponent> group : groupedComposites.values()) {
			if (!group.isEmpty()) {
				// All components in a group share the same type and compositeKey.
				// Try to form a CompositeAttribute from the globally collected group.
				CompositeAttribute.of(group.get(0).type(), group.get(0).compositeKey(), group)
						.ifPresentOrElse(
								bakedAttributes::add, // Successfully formed, add it to the final list.
								() -> { /* If it fails, discard the entire group as per the design. */ }
						);
			}
		}

		return bakedAttributes;
	}
}
