package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that is true if the component is located at a specific depth in the component tree.
 * The root is at depth 0, its direct children are at depth 1, and so on.
 */
public record AtDepthCondition(OpenIdentifier type, int depth) implements StaticCondition {
	public static final Codec<AtDepthCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AtDepthCondition::type),
					Codec.INT.fieldOf("depth").forGetter(AtDepthCondition::depth)
			).apply(instance, AtDepthCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.getDepth() == depth;
	}
}
