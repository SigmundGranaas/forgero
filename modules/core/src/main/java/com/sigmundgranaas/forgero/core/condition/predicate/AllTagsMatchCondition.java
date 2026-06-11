package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Condition that tests if a component has ALL of a set of tags (AND logic).
 * Enables querying for components at the intersection of multiple tag axes.
 *
 * Example: Find materials that are both "metal" AND "tool_material" AND "durable"
 * {
 *   "type": "forgero:self_has_all_tags",
 *   "tags": [
 *     "forgero:materials/types/metal",
 *     "forgero:materials/roles/tool_material",
 *     "forgero:materials/properties/durable"
 *   ]
 * }
 */
public final class AllTagsMatchCondition implements StaticCondition {
	public static final String SELF_HAS_ALL_TAGS = "forgero:self_has_all_tags";
	public static final String ROOT_HAS_ALL_TAGS = "forgero:root_has_all_tags";

	private final OpenIdentifier type;
	private final List<OpenIdentifier> tags;
	private final transient Supplier<TagResolver> tagResolverSupplier;

	public AllTagsMatchCondition(OpenIdentifier type, List<OpenIdentifier> tags, Supplier<TagResolver> tagResolverSupplier) {
		this.type = type;
		this.tags = tags;
		this.tagResolverSupplier = tagResolverSupplier;
	}

	public static Codec<AllTagsMatchCondition> codec(Supplier<TagResolver> tagResolverSupplier) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AllTagsMatchCondition::type),
						CodecConstants.OPEN_IDENTIFIER_CODEC.listOf().fieldOf("tags").forGetter(AllTagsMatchCondition::tags)
				).apply(instance, (type, tags) -> new AllTagsMatchCondition(type, tags, tagResolverSupplier))
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

		var target = type.path().equals("self_has_all_tags") ? context.self() : context.root();

		// Component must have ALL specified tags (AND logic)
		return tags.stream().allMatch(tag -> resolver.hasTag(target, tag));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllTagsMatchCondition that = (AllTagsMatchCondition) o;
		return Objects.equals(type, that.type) && Objects.equals(tags, that.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, tags);
	}
}
