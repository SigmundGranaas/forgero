package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.List;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * A baking strategy that handles slot-scoped attribute filtering for structured components.
 *
 * <p><b>Slot-Scoped Filtering</b></p>
 * This strategy collects all attributes from all components in the tree, filtering them
 * based on their static conditions (including {@code when_in} slot type filters).
 * The {@code when_in} filter is converted to an {@code InSlotTypeCondition} during data loading,
 * ensuring that attributes only apply when their owning component is placed in the correct slot type.
 *
 * <p><b>Composition Model</b></p>
 * Unlike the previous composite-key system, composition now happens naturally through:
 * <ul>
 *   <li>Slot filtering: Material attributes only apply when in material slots</li>
 *   <li>Operator ordering: Modifiers use appropriate operators (multiply, etc.)</li>
 *   <li>ComputationChain: Handles ordered application of operators</li>
 * </ul>
 *
 * <p><b>Example</b></p>
 * A pickaxe_head with a material slot:
 * <ul>
 *   <li>Material (iron) provides: attack_damage = 6 (when_in: material)</li>
 *   <li>Part template (pickaxe_head) provides: attack_damage = multiply 1.5</li>
 *   <li>Baked attributes: [6 (add, group 0), 1.5 (multiply, group 1)]</li>
 *   <li>ComputationChain applies: (0 + 6) * 1.5 = 9</li>
 * </ul>
 */
public class CompositeAttributeBakingStrategy implements AttributeBakingStrategy {

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return List.of();
		}
		Component root = componentList.get(0);

		// Collect all attributes from all components that pass their static conditions
		// The when_in filter (converted to InSlotTypeCondition) will filter out
		// attributes that aren't in the correct slot type
		return componentList.stream()
				.flatMap(component -> {
					ResolutionContext resCtx = new ResolutionContext(component, root);
					return component.properties(KEY).stream()
							.filter(attribute -> attribute.condition()
									.map(Condition::staticConditions)
									.map(resCtx::test)
									.orElse(true)); // If no static conditions, it passes.
				})
				.toList();
	}
}
