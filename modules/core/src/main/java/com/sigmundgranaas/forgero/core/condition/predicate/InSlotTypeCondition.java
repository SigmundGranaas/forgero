package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Optional;

/**
 * A static condition that is true if the component providing the property ('self') is contained
 * within a slot of a specific type.
 */
public record InSlotTypeCondition(OpenIdentifier type, OpenIdentifier slotType) implements StaticCondition {
	public static final Codec<InSlotTypeCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(InSlotTypeCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(InSlotTypeCondition::slotType)
			).apply(instance, InSlotTypeCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		// Check if component is in a mutable slot (upgrade slot)
		Optional<Boolean> slotCheck = context.getSlot()
				.map(slot -> slot.slotType().equals(slotType));
		if (slotCheck.isPresent()) {
			return slotCheck.get();
		}

		// Check if component is in an immutable structure part
		return context.getPart()
				.map(part -> part.partType().equals(slotType))
				.orElse(false);
	}
}
