package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Composite filter that requires ANY child filter to pass.
 * Implements logical OR operation.
 */
public record OrFilter(List<EntityFilter> filters) implements EntityFilter {
	public static final String TYPE = "forgero:or";
	public static final Codec<OrFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(EntityFilter.CODEC).fieldOf("filters").forGetter(OrFilter::filters)
	).apply(instance, OrFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		for (EntityFilter filter : filters) {
			if (filter.test(source, candidate)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
