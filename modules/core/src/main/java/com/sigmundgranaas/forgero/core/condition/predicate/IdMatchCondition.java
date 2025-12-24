package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that matches if the component's identifier matches the given ID.
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
