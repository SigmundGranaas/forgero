package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

/**
 * Filters entities based on whether they are teammates of the source.
 * Can be inverted to filter only non-teammates.
 */
public record IsTeammateFilter(boolean invert) implements EntityFilter {
	public static final String TYPE = "forgero:is_teammate";
	public static final Codec<IsTeammateFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("invert", false).forGetter(IsTeammateFilter::invert)
	).apply(instance, IsTeammateFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		boolean isTeammate = candidate.isTeammate(source);
		return invert ? !isTeammate : isTeammate;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
