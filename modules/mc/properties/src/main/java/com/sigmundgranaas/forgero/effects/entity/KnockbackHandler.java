package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

public record KnockbackHandler(float force, Direction direction) implements ContextualEffectHandler {
	public static final String TYPE = "forgero:knockback";
	public static final Codec<KnockbackHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("force").forGetter(KnockbackHandler::force),
			Direction.CODEC.fieldOf("direction").forGetter(KnockbackHandler::direction)
	).apply(instance, KnockbackHandler::new));

	@Override
	public void apply(Entity source, Entity target) {
		Vec3d vec = target.getPos().subtract(source.getPos()).normalize();
		if (direction == Direction.PULL) {
			vec = vec.multiply(-1);
		}
		target.addVelocity(vec.x * force, vec.y * force, vec.z * force);
	}

	@Override
	public String type() {
		return TYPE;
	}

	public enum Direction implements StringIdentifiable {
		PUSH, PULL;

		public static final Codec<Direction> CODEC = StringIdentifiable.createCodec(Direction::values, (value) -> String.valueOf(Direction.valueOf(value.toUpperCase(Locale.ROOT))));

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
