package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.PrecomputedAttribute;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The expert engine for resolving Attributes from component trees.
 *
 * <p>This engine implements the two-phase resolution pattern with aggressive optimization
 * for the common case: attributes without dynamic conditions.
 *
 * <h2>Performance Optimization Strategy</h2>
 *
 * <p>The engine uses <strong>pre-computed conditionals</strong> to minimize per-query work:
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │  BAKE PHASE (Runs Once Per Component State)                                │
 * │                                                                             │
 * │  1. Traverse component tree                                                │
 * │  2. Collect raw attributes (apply static conditions)                       │
 * │  3. Group by attribute type (attack_damage, durability, etc.)              │
 * │  4. For each type:                                                         │
 * │     a. Partition into unconditional vs conditional (has dynamic conditions)│
 * │     b. Pre-compute base value from unconditional via ComputationChain      │
 * │     c. Store conditional list for runtime evaluation                       │
 * │  5. Return BakedAttributes (Map&lt;Type, PrecomputedAttribute&gt;)              │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *                                    ↓
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │  APPLY PHASE (Runs Per Query)                                              │
 * │                                                                             │
 * │  For query: result.getValue(ATTACK_DAMAGE)                                 │
 * │                                                                             │
 * │  1. O(1) map lookup: baked.get(ATTACK_DAMAGE) → PrecomputedAttribute       │
 * │  2. IF no conditionals: return baseValue immediately (FAST PATH)           │
 * │  3. ELSE: filter conditionals by DynamicContext, apply via ComputationChain│
 * └─────────────────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Complexity Analysis</h2>
 * <table>
 *   <tr><th>Operation</th><th>Before (List&lt;Attribute&gt;)</th><th>After (BakedAttributes)</th></tr>
 *   <tr><td>Query one type</td><td>O(n) - iterate all</td><td>O(1) + O(m) conditionals</td></tr>
 *   <tr><td>Query k types</td><td>O(k*n)</td><td>O(k) + O(m) per type</td></tr>
 *   <tr><td>No conditionals</td><td>O(n)</td><td>O(1) - instant return</td></tr>
 * </table>
 * <p>Where n = total attributes, m = conditional attributes for queried type (typically m &lt;&lt; n)
 *
 * <h2>Strategy Pattern</h2>
 * <p>This engine uses strategy pattern for the bake phase to handle different component types:
 * <ul>
 *   <li>{@link DefaultBakingStrategyImpl} - Simple attribute collection with static filtering</li>
 *   <li>{@link CompositeAttributeBakingStrategy} - Part composition (intersection-based combining)</li>
 * </ul>
 *
 * <h2>Type Parameters</h2>
 * <ul>
 *   <li>{@code <B>}: {@link BakedAttributes} - Map-based structure with O(1) type lookup</li>
 *   <li>{@code <R>}: {@link AttributeQueryResult} - Lightweight query interface</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * AttributeEngine engine = new AttributeEngine();
 *
 * // Simple usage (most common)
 * AttributeQueryResult result = engine.resolve(pickaxe);
 * float damage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
 *
 * // With dynamic context (for conditional attributes)
 * DynamicContext ctx = new DynamicContext.Builder()
 *     .put(IS_SNEAKING, true)
 *     .build();
 * AttributeQueryResult contextual = engine.resolve(pickaxe, ctx);
 * float sneakDamage = contextual.getValue(DefaultAttributes.ATTACK_DAMAGE);
 * }</pre>
 *
 * @see BakedAttributes for the optimized baked structure
 * @see PrecomputedAttribute for per-type pre-computation
 * @see DataTypeEngine for the two-phase resolution interface
 */
public class AttributeEngine implements DataTypeEngine<BakedAttributes, AttributeQueryResult> {
	public static final ResolutionKey<AttributeQueryResult> KEY = new ResolutionKey<>(new OpenIdentifier("forgero", "attributes"));

	// Initialize the concrete strategies
	private final AttributeBakingStrategy defaultBakingStrategy = new DefaultBakingStrategyImpl();
	private final AttributeBakingStrategy compositeBakingStrategy = new CompositeAttributeBakingStrategy();

	public AttributeEngine() {
	}

	@Override
	public ResolutionKey<AttributeQueryResult> key() {
		return KEY;
	}

	@Override
	public BakedAttributes bake(Stream<Component> components) {
		// Convert the stream to a list to be able to inspect the first element (root component).
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return BakedAttributes.EMPTY;
		}

		// Determine which baking strategy to use based on the type of the root component.
		// Use CompositeAttributeBakingStrategy for:
		// - StructuredComponent: needs part-composite context handling for composition
		// - CustomizableComponent: needs upgrade attribute filtering by context
		Component root = componentList.get(0);
		AttributeBakingStrategy strategyToUse =
				(root instanceof StructuredComponent || root instanceof CustomizableComponent)
						? compositeBakingStrategy
						: defaultBakingStrategy;

		// Delegate to the chosen strategy to get raw attributes (with static conditions applied).
		List<Attribute> rawAttributes = strategyToUse.bake(componentList.stream());
		if (rawAttributes.isEmpty()) {
			return BakedAttributes.EMPTY;
		}

		// Transform raw attributes into optimized BakedAttributes structure.
		return buildBakedAttributes(rawAttributes);
	}

	/**
	 * Transforms a list of raw attributes into the optimized BakedAttributes structure.
	 * <p>
	 * For each attribute type:
	 * <ol>
	 *   <li>Partition into unconditional (no dynamic conditions) and conditional</li>
	 *   <li>Pre-compute base value from unconditional attributes using ComputationChain</li>
	 *   <li>Store conditional attributes for runtime evaluation</li>
	 * </ol>
	 *
	 * @param attributes The raw attributes to transform
	 * @return Optimized BakedAttributes with O(1) type lookup
	 */
	private BakedAttributes buildBakedAttributes(List<Attribute> attributes) {
		// Group attributes by type
		Map<OpenIdentifier, List<Attribute>> byType = attributes.stream()
				.collect(Collectors.groupingBy(Attribute::type));

		Map<OpenIdentifier, PrecomputedAttribute> result = new HashMap<>();

		for (var entry : byType.entrySet()) {
			OpenIdentifier type = entry.getKey();
			List<Attribute> attrs = entry.getValue();

			// Partition: unconditional (no dynamic conditions) vs conditional
			List<Attribute> unconditional = new ArrayList<>();
			List<Attribute> conditional = new ArrayList<>();

			for (Attribute attr : attrs) {
				if (hasDynamicConditions(attr)) {
					conditional.add(attr);
				} else {
					unconditional.add(attr);
				}
			}

			// Pre-compute base value from unconditional attributes
			float baseValue = unconditional.isEmpty()
					? 0f
					: new ComputationChain(unconditional).compute(0f);

			result.put(type, new PrecomputedAttribute(baseValue, conditional));
		}

		return new BakedAttributes(result);
	}

	/**
	 * Returns true if the attribute has any dynamic conditions that require runtime evaluation.
	 */
	private boolean hasDynamicConditions(Attribute attr) {
		return attr.condition()
				.map(c -> !c.dynamicConditions().isEmpty())
				.orElse(false);
	}

	@Override
	public AttributeQueryResult apply(BakedAttributes baked, DynamicContext context) {
		// Return a lightweight query object with O(1) type lookup + conditional evaluation.
		return (attributeType) -> baked.get(attributeType).compute(context);
	}
}
