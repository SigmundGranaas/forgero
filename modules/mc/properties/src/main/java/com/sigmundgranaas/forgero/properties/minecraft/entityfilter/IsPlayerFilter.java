package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Filters entities that are players.
 */
public record IsPlayerFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_player";
	public static final IsPlayerFilter INSTANCE = new IsPlayerFilter();
	public static final Codec<IsPlayerFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate instanceof PlayerEntity;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
