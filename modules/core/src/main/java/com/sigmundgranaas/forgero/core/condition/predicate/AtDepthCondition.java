package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that passes when the component is at a specific depth in the component tree.
 *
 * <p>Depth is measured from the root:
 * <ul>
 *   <li>Depth 0: Root component (the equipment item)</li>
 *   <li>Depth 1: Direct children of root (head, handle)</li>
 *   <li>Depth 2: Grandchildren (material inside part)</li>
 *   <li>And so on...</li>
 * </ul>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Level-specific properties:</strong> Apply different effects based on how
 *       deep in the hierarchy the component is</li>
 *   <li><strong>Filtering materials:</strong> Only apply to base materials (depth 2+)
 *       but not to assembled parts (depth 1)</li>
 *   <li><strong>Nested upgrade behavior:</strong> Upgrades in nested slots behave differently</li>
 * </ul>
 *
 * <h2>Component Tree Depth Example</h2>
 * <pre>
 * iron-pickaxe                  ← depth 0 (root)
 *   ├─ iron-pickaxe_head        ← depth 1
 *   │   ├─ pickaxe_schematic    ← depth 2
 *   │   └─ iron                 ← depth 2
 *   ├─ oak-handle               ← depth 1
 *   └─ diamond_gem              ← depth 1 (upgrade)
 *       └─ diamond              ← depth 2 (material inside gem)
 * </pre>
 *
 * <h2>Example: Material-Level Only</h2>
 * <pre>{@code
 * {
 *   "attributes": [{
 *     "type": "forgero:durability",
 *     "value": 100,
 *     "condition": {
 *       "static": [{ "type": "forgero:at_depth", "depth": 2 }]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * @param type  The condition type identifier (always "forgero:at_depth")
 * @param depth The exact depth to match (0 = root, 1 = children, etc.)
 * @see ResolutionContext#getDepth() for the underlying depth check
 * @see IsRootCondition for a simpler "is at depth 0" check
 */
public record AtDepthCondition(OpenIdentifier type, int depth) implements StaticCondition {
	public static final Codec<AtDepthCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AtDepthCondition::type),
					Codec.INT.fieldOf("depth").forGetter(AtDepthCondition::depth)
			).apply(instance, AtDepthCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.getDepth() == depth;
	}
}
