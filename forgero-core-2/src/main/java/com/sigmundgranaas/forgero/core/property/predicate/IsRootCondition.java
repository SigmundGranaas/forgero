package com.sigmundgranaas.forgero.core.property.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that is true only if the component being evaluated is the root of the resolution tree.
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
