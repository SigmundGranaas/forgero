package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A static condition that passes when a component (self or root) has a specific tag.
 *
 * <p>Tags are hierarchical identifiers that classify components. This condition uses
 * the {@link TagResolver} to check tag membership, which supports tag inheritance
 * (if a component has tag "forgero:metal", it also has "forgero:material").
 *
 * <h2>Condition Types</h2>
 * <table>
 *   <tr><th>Type</th><th>Checks</th><th>Use Case</th></tr>
 *   <tr><td>{@code forgero:self_has_tag}</td><td>Current component</td><td>Material-specific properties</td></tr>
 *   <tr><td>{@code forgero:root_has_tag}</td><td>Root equipment</td><td>Equipment-type bonuses</td></tr>
 * </table>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Material-specific bonuses:</strong> Extra durability for metal materials</li>
 *   <li><strong>Category filtering:</strong> Apply effect only to weapons, not tools</li>
 *   <li><strong>Synergy effects:</strong> Bonus when root is a specific equipment type</li>
 * </ul>
 *
 * <h2>Example: Metal Material Bonus</h2>
 * <pre>{@code
 * // Extra durability for metal materials:
 * {
 *   "attributes": [{
 *     "type": "forgero:durability",
 *     "value": 1.1,
 *     "operator": "multiplication",
 *     "condition": {
 *       "static": [{ "type": "forgero:self_has_tag", "tag": "forgero:metal" }]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <h2>Example: Weapon-Only Effect</h2>
 * <pre>{@code
 * // Fire damage only on weapons:
 * {
 *   "on_hit": [{
 *     "selector": { "type": "forgero:single_target" },
 *     "effects": [{ "type": "forgero:fire", "duration": 100 }],
 *     "condition": {
 *       "static": [{ "type": "forgero:root_has_tag", "tag": "forgero:weapon" }]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <h2>Common Tags</h2>
 * <ul>
 *   <li>{@code forgero:metal} - Metal materials (iron, gold, netherite)</li>
 *   <li>{@code forgero:wood} - Wood materials (oak, birch, etc.)</li>
 *   <li>{@code forgero:stone} - Stone materials</li>
 *   <li>{@code forgero:gem} - Gem materials (diamond, emerald)</li>
 *   <li>{@code forgero:weapon} - Weapon equipment types</li>
 *   <li>{@code forgero:tool} - Tool equipment types</li>
 *   <li>{@code forgero:pickaxe}, {@code forgero:sword}, etc. - Specific equipment types</li>
 * </ul>
 *
 * @see AllTagsMatchCondition for AND logic across multiple tags
 * @see AnyTagMatchCondition for OR logic across multiple tags
 * @see TagResolver for tag inheritance and resolution
 */
public final class TagMatchCondition implements StaticCondition {

	private final OpenIdentifier type;
	private final OpenIdentifier tag;
	private final transient Supplier<TagResolver> tagResolverSupplier;

	public TagMatchCondition(OpenIdentifier type, OpenIdentifier tag, Supplier<TagResolver> tagResolverSupplier) {
		this.type = type;
		this.tag = tag;
		this.tagResolverSupplier = tagResolverSupplier;
	}

	public static Codec<TagMatchCondition> codec(Supplier<TagResolver> tagResolverSupplier) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(TagMatchCondition::type),
						// Tags are path-preserving (e.g. forgero:tools/types/hoe). Canonicalizing the
						// operand would collapse it to its last segment and never match the graph node.
						CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TagMatchCondition::tag)
				).apply(instance, (type, tag) -> new TagMatchCondition(type, tag, tagResolverSupplier))
		);
	}

	@Override
	public OpenIdentifier type() {
		return type;
	}

	public OpenIdentifier tag() {
		return tag;
	}

	@Override
	public boolean test(ResolutionContext context) {
		TagResolver resolver = tagResolverSupplier.get();
		if (resolver == null) {
			// Fail-safe in case the supplier was not injected correctly or is not available.
			return false;
		}

		if (type.path().equals("self_has_tag")) {
			return resolver.hasTag(context.self(), tag);
		} else if (type.path().equals("root_has_tag")) {
			return resolver.hasTag(context.root(), tag);
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagMatchCondition that = (TagMatchCondition) o;
		return Objects.equals(type, that.type) && Objects.equals(tag, that.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, tag);
	}
}
