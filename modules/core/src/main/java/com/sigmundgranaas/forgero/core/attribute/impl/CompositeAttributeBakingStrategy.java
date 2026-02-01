package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeScopeHandler;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTreeWalker;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;

import java.util.*;
import java.util.stream.Stream;

/**
 * Baking strategy for scope-based attribute composition in structured components.
 *
 * <p>This implementation uses a visitor pattern with specialized collectors to
 * traverse the component tree in a single pass, collecting different types of
 * attributes simultaneously.</p>
 *
 * <h2>Collectors Used</h2>
 * <ul>
 *   <li>{@link PartCompositeAttributeCollector} - Collects PART_COMPOSITE scope attributes</li>
 *   <li>{@link DefaultAttributeCollector} - Collects default (no-scope) attributes</li>
 *   <li>{@link UpgradeAttributeCollector} - Collects attributes from upgrade slots</li>
 * </ul>
 *
 * <h2>Attribute Flow for Part-Constructed Components</h2>
 * <ol>
 *   <li><b>Filter by scope</b> - Collect attributes with part-composite scope</li>
 *   <li><b>Apply conditions</b> - Filter attributes by their static conditions (e.g., in_slot_type)</li>
 *   <li><b>Combine and transform</b> - Compose attributes using intersection logic (base × multiplier)</li>
 *   <li><b>Discard untransformed</b> - Remove any attributes with scope that weren't composed</li>
 *   <li><b>Pass through defaults</b> - Attributes without scope pass through unchanged</li>
 * </ol>
 *
 * <h2>Example: Iron Pickaxe Head</h2>
 * <pre>
 * Material (iron):
 *   - mining_speed: 6.0 (scope=part-composite, condition=in_slot_type:tool_material)
 *   - armor: 2.0 (scope=part-composite, condition=in_slot_type:armor_material)
 *
 * Shape (pickaxe_head):
 *   - mining_speed: ×1.2 (scope=part-composite)
 *
 * Result:
 *   - mining_speed: 7.2 (composed: 6.0 × 1.2)
 *   - armor: excluded (condition failed - wrong slot type)
 * </pre>
 *
 * @see PartCompositeAttributeCollector
 * @see DefaultAttributeCollector
 * @see UpgradeAttributeCollector
 * @see com.sigmundgranaas.forgero.core.component.api.ComponentTreeWalker
 */
public class CompositeAttributeBakingStrategy implements AttributeBakingStrategy {

	private final PartCompositeScopeHandler partCompositeHandler = PartCompositeScopeHandler.INSTANCE;

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return List.of();
		}
		Component root = componentList.get(0);
		List<Attribute> result = new ArrayList<>();

		// Create collectors for single-pass traversal
		PartCompositeAttributeCollector partCompositeCollector = new PartCompositeAttributeCollector();
		DefaultAttributeCollector defaultCollector = new DefaultAttributeCollector();
		UpgradeAttributeCollector upgradeCollector = new UpgradeAttributeCollector();

		// Single-pass traversal with all collectors
		ComponentTreeWalker.walkMultiple(root, List.of(
				partCompositeCollector,
				defaultCollector,
				upgradeCollector
		));

		// Handle structured component composition
		if (root instanceof StructuredComponent) {
			Map<String, List<Attribute>> partCompositeSources = partCompositeCollector.getResult();
			result.addAll(composeFromSources(partCompositeSources));
		}

		// Add default attributes (pass through unchanged)
		result.addAll(defaultCollector.getResult());

		// Add upgrade attributes (already resolved by collector)
		result.addAll(upgradeCollector.getResult());

		return result;
	}

	/**
	 * Compose attributes from multiple sources using intersection or additive logic.
	 */
	private List<Attribute> composeFromSources(Map<String, List<Attribute>> sources) {
		if (sources.size() >= 2) {
			List<Attribute> composed = partCompositeHandler.compose(sources);
			if (composed.isEmpty()) {
				composed = additiveComposeMultiSource(sources);
			}
			return composed;
		} else if (sources.size() == 1) {
			return discardScopedAttributes(sources.values().iterator().next());
		}
		return List.of();
	}

	private List<Attribute> additiveComposeMultiSource(Map<String, List<Attribute>> sources) {
		Map<OpenIdentifier, Set<String>> typeToSources = new HashMap<>();
		Map<OpenIdentifier, Float> sums = new HashMap<>();
		Map<OpenIdentifier, Boolean> hasResolved = new HashMap<>();

		for (Map.Entry<String, List<Attribute>> entry : sources.entrySet()) {
			String sourceName = entry.getKey();
			for (Attribute attr : entry.getValue()) {
				OpenIdentifier type = attr.type();
				sums.merge(type, attr.value(), Float::sum);

				if (attr.scope().isEmpty()) {
					hasResolved.put(type, true);
				} else {
					typeToSources.computeIfAbsent(type, k -> new HashSet<>()).add(sourceName);
				}
			}
		}

		return sums.entrySet().stream()
				.filter(e -> {
					OpenIdentifier type = e.getKey();
					if (hasResolved.getOrDefault(type, false)) {
						return true;
					}
					Set<String> scopeSources = typeToSources.get(type);
					return scopeSources != null && scopeSources.size() >= 2;
				})
				.<Attribute>map(e -> SimpleAttribute.resolved(e.getKey(), e.getValue()))
				.toList();
	}

	private List<Attribute> discardScopedAttributes(List<Attribute> attributes) {
		return attributes.stream()
				.filter(attr -> attr.scope().isEmpty())
				.toList();
	}
}
