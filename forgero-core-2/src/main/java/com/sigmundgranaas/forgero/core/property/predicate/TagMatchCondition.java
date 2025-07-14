package com.sigmundgranaas.forgero.core.property.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

public record TagMatchCondition(OpenIdentifier type, OpenIdentifier tag) implements StaticCondition {

	public static final Codec<TagMatchCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(TagMatchCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TagMatchCondition::tag)
			).apply(instance, TagMatchCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		if (type.path().equals("self_has_tag")) {
			return context.self().getTags().stream().anyMatch(t -> t.equals(tag));
		} else if (type.path().equals("root_has_tag")) {
			return context.root().getTags().stream().anyMatch(t -> t.equals(tag));
		}
		return false;
	}
}
