package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that is true if a slot of a given type is filled with a component that has a specific tag.
 * This allows for powerful cross-component conditional logic.
 */
public record SlotContainsCondition(OpenIdentifier type, OpenIdentifier slotType, OpenIdentifier tag) implements StaticCondition {
	public static final Codec<SlotContainsCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SlotContainsCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(SlotContainsCondition::slotType),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(SlotContainsCondition::tag)
			).apply(instance, SlotContainsCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.findInRoot(slotType)
				.map(comp -> comp.getTags().contains(tag))
				.orElse(false);
	}
}
