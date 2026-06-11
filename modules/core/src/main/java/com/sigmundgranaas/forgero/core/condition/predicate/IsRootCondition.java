package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that passes only when the component is the root of the resolution tree.
 *
 * <p>The "root" is the top-level component being resolved - typically the final assembled
 * equipment item (pickaxe, sword, bow). Child components (parts, materials, upgrades)
 * are not the root, even if they define this condition.
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Equipment-only attributes:</strong> Attack speed should only apply to the
 *       final weapon, not to individual blade or handle parts</li>
 *   <li><strong>Preventing attribute leakage:</strong> Ensure properties don't accidentally
 *       apply when a component is used as a part in a larger assembly</li>
 *   <li><strong>Final assembly bonuses:</strong> Synergy effects that only activate when
 *       all parts are combined</li>
 * </ul>
 *
 * <h2>Example: Equipment-Only Attack Speed</h2>
 * <pre>{@code
 * // In sword.json equipment definition:
 * {
 *   "attributes": [{
 *     "type": "forgero:attack_speed",
 *     "value": -2.4,
 *     "condition": {
 *       "static": [{ "type": "forgero:is_root" }]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <p>This ensures the attack speed modifier only applies when resolving the sword itself,
 * not when the sword's blade is inspected independently.
 *
 * <h2>Component Tree Example</h2>
 * <pre>
 * iron-pickaxe (ROOT)          ← isRoot() = true
 *   ├─ iron-pickaxe_head       ← isRoot() = false
 *   │   └─ iron material       ← isRoot() = false
 *   └─ oak-handle              ← isRoot() = false
 * </pre>
 *
 * @param type The condition type identifier (always "forgero:is_root")
 * @see ResolutionContext#isRoot() for the underlying check
 */
public record IsRootCondition(OpenIdentifier type) implements StaticCondition {
	public static final Codec<IsRootCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(IsRootCondition::type)
			).apply(instance, IsRootCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.isRoot();
	}
}
