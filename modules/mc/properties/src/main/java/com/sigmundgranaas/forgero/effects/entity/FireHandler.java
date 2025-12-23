package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

public record FireHandler(int duration) implements EntityEffectHandler {
	public static final String TYPE = "forgero:fire";
	public static final Codec<FireHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("duration").forGetter(FireHandler::duration)
	).apply(instance, FireHandler::new));

	@Override
	public void apply(Entity entity) {
		entity.setOnFireFor(duration);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
