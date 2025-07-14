package com.sigmundgranaas.forgero.core.property.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that is true if the component has a sibling with a specific ID.
 * "Siblings" are other components that share the same direct parent.
 */
public record HasSiblingCondition(OpenIdentifier type, OpenIdentifier siblingId) implements StaticCondition {
	public static final Codec<HasSiblingCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(HasSiblingCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("sibling_id").forGetter(HasSiblingCondition::siblingId)
			).apply(instance, HasSiblingCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.getSiblings().stream().anyMatch(sibling -> sibling.id().equals(siblingId));
	}
}
