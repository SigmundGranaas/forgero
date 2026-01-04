package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Condition that tests if a component has ANY of a set of tags (OR logic).
 * Enables querying for components that match at least one tag from multiple axes.
 *
 * Example: Find materials that are either "metal" OR "stone" OR "gem"
 * {
 *   "type": "forgero:self_has_any_tag",
 *   "tags": [
 *     "forgero:materials/types/metal",
 *     "forgero:materials/types/stone",
 *     "forgero:materials/types/gem"
 *   ]
 * }
 */
public final class AnyTagMatchCondition implements StaticCondition {
	public static final String SELF_HAS_ANY_TAG = "forgero:self_has_any_tag";
	public static final String ROOT_HAS_ANY_TAG = "forgero:root_has_any_tag";

	private final OpenIdentifier type;
	private final List<OpenIdentifier> tags;
	private final transient Supplier<TagResolver> tagResolverSupplier;

	public AnyTagMatchCondition(OpenIdentifier type, List<OpenIdentifier> tags, Supplier<TagResolver> tagResolverSupplier) {
		this.type = type;
		this.tags = tags;
		this.tagResolverSupplier = tagResolverSupplier;
	}

	public static Codec<AnyTagMatchCondition> codec(Supplier<TagResolver> tagResolverSupplier) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AnyTagMatchCondition::type),
						CodecConstants.OPEN_IDENTIFIER_CODEC.listOf().fieldOf("tags").forGetter(AnyTagMatchCondition::tags)
				).apply(instance, (type, tags) -> new AnyTagMatchCondition(type, tags, tagResolverSupplier))
		);
	}

	@Override
	public OpenIdentifier type() {
		return type;
	}

	public List<OpenIdentifier> tags() {
		return tags;
	}

	@Override
	public boolean test(ResolutionContext context) {
		TagResolver resolver = tagResolverSupplier.get();
		if (resolver == null || tags == null || tags.isEmpty()) {
			return false;
		}

		var target = type.path().equals("self_has_any_tag") ? context.self() : context.root();

		// Component must have AT LEAST ONE of the specified tags (OR logic)
		return tags.stream().anyMatch(tag -> resolver.hasTag(target, tag));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AnyTagMatchCondition that = (AnyTagMatchCondition) o;
		return Objects.equals(type, that.type) && Objects.equals(tags, that.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, tags);
	}
}
