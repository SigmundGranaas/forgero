package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

/**
 * Composite filter that inverts the result of a child filter.
 * Implements logical NOT operation.
 */
public record NotFilter(EntityFilter filter) implements EntityFilter {
	public static final String TYPE = "forgero:not";
	public static final Codec<NotFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityFilter.CODEC.fieldOf("filter").forGetter(NotFilter::filter)
	).apply(instance, NotFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		return !filter.test(source, candidate);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
