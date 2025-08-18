package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Objects;
import java.util.function.Supplier;

public final class TagMatchCondition implements StaticCondition {

	private final OpenIdentifier type;
	private final OpenIdentifier tag;
	private final transient Supplier<TagGraph> tagGraphSupplier;

	public TagMatchCondition(OpenIdentifier type, OpenIdentifier tag, Supplier<TagGraph> tagGraphSupplier) {
		this.type = type;
		this.tag = tag;
		this.tagGraphSupplier = tagGraphSupplier;
	}

	public static Codec<TagMatchCondition> codec(Supplier<TagGraph> tagGraphSupplier) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(TagMatchCondition::type),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TagMatchCondition::tag)
				).apply(instance, (type, tag) -> new TagMatchCondition(type, tag, tagGraphSupplier))
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
		TagGraph graph = tagGraphSupplier.get();
		if (graph == null) {
			// Fail-safe in case the supplier was not injected correctly or is not available.
			return false;
		}

		if (type.path().equals("self_has_tag")) {
			return graph.isTagged(context.self(), tag);
		} else if (type.path().equals("root_has_tag")) {
			return graph.isTagged(context.root(), tag);
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
