package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;

public record FireHandler(int duration) implements OnHitHandler {
	public static final String TYPE = "forgero:fire";
	public static final Codec<FireHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("duration").forGetter(FireHandler::duration)
	).apply(instance, FireHandler::new));

	@Override
	public void onHit(Entity source, Entity target) {
		target.setFireTicks(duration * 20); // duration in seconds
	}

	@Override
	public String type() {
		return TYPE;
	}
}
