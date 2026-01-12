package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that passes when the component's ID matches a specific identifier.
 *
 * <p>This condition performs exact ID matching against the current component. The ID
 * can be specified with or without the "forgero:" namespace prefix - both forms are accepted.
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Component-specific overrides:</strong> Special behavior for one specific material</li>
 *   <li><strong>Unique item bonuses:</strong> Properties that only apply to a named component</li>
 *   <li><strong>Testing/debugging:</strong> Target a specific component for testing</li>
 * </ul>
 *
 * <h2>ID Matching Rules</h2>
 * <ul>
 *   <li>Exact match: {@code "forgero:diamond"} matches component with ID "forgero:diamond"</li>
 *   <li>Shorthand: {@code "diamond"} also matches "forgero:diamond" (namespace auto-added)</li>
 *   <li>Case-sensitive: IDs must match exactly including case</li>
 * </ul>
 *
 * <h2>Example: Netherite-Specific Bonus</h2>
 * <pre>{@code
 * // Extra fire resistance only for netherite material:
 * {
 *   "attributes": [{
 *     "type": "forgero:fire_resistance",
 *     "value": 0.5,
 *     "condition": {
 *       "static": [{ "type": "forgero:id_match", "id": "netherite" }]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <h2>Comparison with Tag Conditions</h2>
 * <p>Use {@code id_match} for targeting a single specific component. Use
 * {@link TagMatchCondition} for targeting categories of components (all metals,
 * all weapons, etc.). Tags are more flexible for content pack compatibility.
 *
 * @param type The condition type identifier (always "forgero:id_match")
 * @param id   The component ID to match (with or without "forgero:" prefix)
 * @see TagMatchCondition for matching by tag instead of exact ID
 */
public record IdMatchCondition(OpenIdentifier type, String id) implements StaticCondition {
	public static final Codec<IdMatchCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(IdMatchCondition::type),
					Codec.STRING.fieldOf("id").forGetter(IdMatchCondition::id)
			).apply(instance, IdMatchCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		String componentId = context.self().id().toString();
		return matchesId(componentId);
	}

	private boolean matchesId(String identifier) {
		return identifier.equals(id) || identifier.equals("forgero:" + id);
	}
}
