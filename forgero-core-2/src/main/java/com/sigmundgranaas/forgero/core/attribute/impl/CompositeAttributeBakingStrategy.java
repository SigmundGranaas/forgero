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
 * An implementation of {@link AttributeBakingStrategy} that processes attributes from each component individually,
 * attempting to form {@link CompositeAttribute}s from {@link CompositeAttributeComponent}s that are local to a single component.
 *
 * <p><b>Core Logic: Local Composition</b></p>
 * This strategy iterates through each component in the hierarchy and performs the following steps:
 * <ol>
 *   <li><b>Gather:</b> It collects all attributes from the current component, filtering them based on their static conditions.</li>
 *   <li><b>Partition:</b> It separates the component's attributes into simple {@link Attribute}s and {@link CompositeAttributeComponent}s. All simple attributes are immediately added to the final baked list.</li>
 *   <li><b>Combine Locally:</b> It groups the {@link CompositeAttributeComponent}s by their type and composite key. Each group, containing components *only from the current component*, is used to attempt to form a single {@link CompositeAttribute}.</li>
 * </ol>
 *
 * <p><b>Grouping</b></p>
 * A {@link CompositeAttribute} is formed from a group of {@link CompositeAttributeComponent}s that share
 * the same {@code type}, {@code compositeKey}, and originate from the <strong>same parent component</strong>.
 * This ensures that composite calculations are encapsulated within the component that defines them.
 *
 * <p><b>Discard on Failure</b></p>
 * If a local group of {@link CompositeAttributeComponent}s fails to meet the criteria for forming a valid
 * {@link CompositeAttribute} (e.g., having fewer than two distinct operators), the entire group of components
 * is <strong>discarded</strong>. They do NOT fall back to being treated as simple attributes.
 *
 * <p><b>Design Rationale</b></p>
 * This "local composition" approach ensures that complex attribute calculations are self-contained and predictable.
 * It prevents unintended interactions between composite components defined on different, unrelated parts of a tool.
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
		List<Attribute> bakedAttributes = new ArrayList<>();

		for (Component component : componentList) {
			ResolutionContext resCtx = new ResolutionContext(component, root);

			// GATHER attributes from the current component and filter by static conditions
			List<Attribute> componentAttributes = component.properties(KEY).stream()
					.filter(attribute -> attribute.condition()
							.map(Condition::staticConditions)
							.map(resCtx::test)
							.orElse(true))
					.toList();

			// PARTITION attributes from this specific component
			Map<Boolean, List<Attribute>> partitionedAttributes = componentAttributes.stream()
					.collect(Collectors.partitioningBy(attr -> attr instanceof CompositeAttributeComponent));

			// Add this component's simple attributes to the final list
			bakedAttributes.addAll(partitionedAttributes.get(false));

			List<CompositeAttributeComponent> localCompositeComponents = partitionedAttributes.get(true)
					.stream()
					.map(CompositeAttributeComponent.class::cast)
					.toList();


			// COMBINE composite components locally for this component
			if (!localCompositeComponents.isEmpty()) {
				Map<String, List<CompositeAttributeComponent>> groupedLocalComposites = localCompositeComponents.stream()
						.collect(Collectors.groupingBy(comp -> comp.type().toString() + ":" + comp.compositeKey().toString()));

				for (List<CompositeAttributeComponent> group : groupedLocalComposites.values()) {
					// A group will never be empty here due to how groupingBy works
					CompositeAttribute.of(group.get(0).type(), group.get(0).compositeKey(), group)
							.ifPresentOrElse(
									bakedAttributes::add, // Successfully formed, add it.
									() -> { /* If it fails, the group is discarded. */ }
							);
				}
			}
		}
		return bakedAttributes;
	}
}
