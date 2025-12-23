package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Composite filter that requires ALL child filters to pass.
 * Implements logical AND operation.
 */
public record AndFilter(List<EntityFilter> filters) implements EntityFilter {
	public static final String TYPE = "forgero:and";
	public static final Codec<AndFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(EntityFilter.CODEC).fieldOf("filters").forGetter(AndFilter::filters)
	).apply(instance, AndFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		for (EntityFilter filter : filters) {
			if (!filter.test(source, candidate)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
