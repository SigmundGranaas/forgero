package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeContextHandler;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.*;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Baking strategy for context-based attribute composition in structured components.
 *
 * <h2>Attribute Flow for Part-Constructed Components</h2>
 * <ol>
 *   <li><b>Filter by context</b> - Collect attributes with part-composite context</li>
 *   <li><b>Apply conditions</b> - Filter attributes by their static conditions (e.g., in_slot_type)</li>
 *   <li><b>Combine and transform</b> - Compose attributes using intersection logic (base × multiplier)</li>
 *   <li><b>Discard untransformed</b> - Remove any attributes with context that weren't composed</li>
 *   <li><b>Pass through defaults</b> - Attributes without context pass through unchanged</li>
 * </ol>
 *
 * <h2>Example: Iron Pickaxe Head</h2>
 * <pre>
 * Material (iron):
 *   - mining_speed: 6.0 (context=part-composite, condition=in_slot_type:tool_material)
 *   - armor: 2.0 (context=part-composite, condition=in_slot_type:armor_material)
 *
 * Shape (pickaxe_head):
 *   - mining_speed: ×1.2 (context=part-composite)
 *
 * Result:
 *   - mining_speed: 7.2 (composed: 6.0 × 1.2)
 *   - armor: excluded (condition failed - wrong slot type)
 * </pre>
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

		if (root instanceof StructuredComponent structured) {
			result.addAll(composeStructuredComponent(structured, root));
		}

		collectDefaultAttributes(componentList, root, result);

		// Collect attributes from upgrades (installed in upgrade slots)
		collectUpgradeAttributes(componentList, root, result);

		return result;
	}

	private List<Attribute> composeStructuredComponent(StructuredComponent structured, Component root) {
		Map<String, List<Attribute>> sources = new HashMap<>();

		List<Attribute> selfAttrs = collectAndFilterContextAttributes(structured, root);
		if (!selfAttrs.isEmpty()) {
			sources.put("self", selfAttrs);
		}

		for (ComponentPart part : structured.structure().allParts()) {
			Component child = part.getContent();
			String slotName = part.id().toString();

			List<Attribute> childAttrs = collectChildAttributes(child, root);
			if (!childAttrs.isEmpty()) {
				sources.put(slotName, childAttrs);
			}
		}

		return composeFromSources(sources);
	}

	/**
	 * Step 1 & 2: Filter attributes by context, then apply conditions.
	 */
	private List<Attribute> collectAndFilterContextAttributes(Component component, Component root) {
		ResolutionContext ctx = new ResolutionContext(component, root);

		return component.properties(KEY).stream()
				.filter(attr -> attr.context()
						.map(c -> c.equals(AttributeContext.PART_COMPOSITE))
						.orElse(false))
				.filter(attr -> attr.condition()
						.map(Condition::staticConditions)
						.map(ctx::test)
						.orElse(true))
				.toList();
	}

	private List<Attribute> collectChildAttributes(Component child, Component root) {
		if (child instanceof StructuredComponent structuredChild) {
			List<Attribute> composed = composeStructuredComponent(structuredChild, root);
			if (!composed.isEmpty()) {
				return composed;
			}
		}
		return collectContextAttributesRecursively(child, root);
	}

	private List<Attribute> collectContextAttributesRecursively(Component component, Component root) {
		List<Attribute> result = new ArrayList<>();
		result.addAll(collectAndFilterContextAttributes(component, root));

		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				result.addAll(collectContextAttributesRecursively(part.getContent(), root));
			}
		}
		return result;
	}

	/**
	 * Step 3 & 4: Combine/transform composite attributes, discard untransformed.
	 */
	private List<Attribute> composeFromSources(Map<String, List<Attribute>> sources) {
		if (sources.size() >= 2) {
			List<Attribute> composed = partCompositeHandler.compose(sources);
			if (composed.isEmpty()) {
				composed = additiveComposeMultiSource(sources);
			}
			return composed;
		} else if (sources.size() == 1) {
			return discardContextAttributes(sources.values().iterator().next());
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

				if (attr.context().isEmpty()) {
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
					Set<String> contextSources = typeToSources.get(type);
					return contextSources != null && contextSources.size() >= 2;
				})
				.<Attribute>map(e -> SimpleAttribute.resolved(e.getKey(), e.getValue()))
				.toList();
	}

	private List<Attribute> discardContextAttributes(List<Attribute> attributes) {
		return attributes.stream()
				.filter(attr -> attr.context().isEmpty())
				.toList();
	}

	/**
	 * Step 5: Pass through default (no context) attributes from the component tree.
	 *
	 * <p>Recursively collects default (no-context) attributes from the root and all
	 * structure children. Upgrade slot contents are NOT processed here - they are handled
	 * separately by {@link #collectUpgradeAttributes} which applies proper context filtering.</p>
	 */
	private void collectDefaultAttributes(List<Component> components, Component root, List<Attribute> result) {
		if (components.isEmpty()) {
			return;
		}

		// Recursively collect from root and all structure children (not upgrades)
		collectDefaultAttributesRecursively(root, root, result);
	}

	/**
	 * Recursively collects default (no-context) attributes from structure children only.
	 *
	 * <p>This method traverses structure parts (ComponentPart) but NOT upgrade slots
	 * (ComponentUpgradeSlot). Upgrade attributes are handled separately with context filtering.</p>
	 */
	private void collectDefaultAttributesRecursively(Component component, Component root, List<Attribute> result) {
		ResolutionContext ctx = new ResolutionContext(component, root);

		// Collect default (no-context) attributes from this component
		List<Attribute> defaults = component.properties(KEY).stream()
				.filter(attr -> attr.context().isEmpty())
				.filter(attr -> attr.condition()
						.map(Condition::staticConditions)
						.map(ctx::test)
						.orElse(true))
				.toList();

		result.addAll(defaults);

		// Recursively process structure parts only (NOT upgrade slots)
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collectDefaultAttributesRecursively(part.getContent(), root, result);
			}
		}
	}

	/**
	 * Collects attributes from upgrades installed in upgrade slots.
	 *
	 * <p>Recursively traverses the component tree to find all CustomizableComponents
	 * and collects attributes from their installed upgrades.</p>
	 *
	 * <p>Attribute filtering rules:</p>
	 * <ul>
	 *   <li>Attributes with no context → always included</li>
	 *   <li>Attributes with "upgrade" context → always included</li>
	 *   <li>Attributes with specific context → only if slot context matches</li>
	 * </ul>
	 *
	 * @param components All components in the tree (typically just the root)
	 * @param root The root component for context resolution
	 * @param result The result list to add attributes to
	 */
	private void collectUpgradeAttributes(List<Component> components, Component root, List<Attribute> result) {
		for (Component component : components) {
			collectUpgradeAttributesRecursively(component, root, result);
		}
	}

	private void collectUpgradeAttributesRecursively(Component component, Component root, List<Attribute> result) {
		// Collect upgrades from this component if it's customizable
		if (component instanceof CustomizableComponent customizable) {
			collectUpgradesFromComponent(customizable, root, result);
		}

		// Recursively check structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collectUpgradeAttributesRecursively(part.getContent(), root, result);
			}
		}
	}

	private void collectUpgradesFromComponent(CustomizableComponent customizable, Component root, List<Attribute> result) {
		for (ComponentUpgradeSlot slot : customizable.upgrades().allUpgradeSlots()) {
			if (!slot.isFilled()) {
				continue;
			}

			Component upgrade = slot.content().orElse(null);
			if (upgrade == null) {
				continue;
			}

			Optional<OpenIdentifier> slotContext = slot.context();
			ResolutionContext ctx = new ResolutionContext(upgrade, root);

			// Collect attributes from the upgrade, filtered by slot context
			List<Attribute> upgradeAttrs = upgrade.properties(KEY).stream()
					.filter(attr -> AttributeContext.matchesSlotContext(attr.context(), slotContext))
					.filter(attr -> attr.condition()
							.map(Condition::staticConditions)
							.map(ctx::test)
							.orElse(true))
					// Resolve the attribute (remove context since it's now been processed)
					.<Attribute>map(attr -> SimpleAttribute.resolved(attr.type(), attr.value()))
					.toList();

			result.addAll(upgradeAttrs);

			// Recursively collect from nested customizable components in the upgrade
			if (upgrade instanceof CustomizableComponent nestedCustomizable) {
				collectUpgradesFromComponent(nestedCustomizable, root, result);
			}
		}
	}
}
