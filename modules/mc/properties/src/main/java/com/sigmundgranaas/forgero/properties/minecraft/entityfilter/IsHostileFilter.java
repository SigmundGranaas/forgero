package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;

/**
 * Filters entities that are hostile mobs (extend HostileEntity).
 */
public record IsHostileFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_hostile";
	public static final IsHostileFilter INSTANCE = new IsHostileFilter();
	public static final Codec<IsHostileFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate instanceof HostileEntity;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
