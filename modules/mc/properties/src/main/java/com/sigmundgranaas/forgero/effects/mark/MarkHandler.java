package com.sigmundgranaas.forgero.effects.mark;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Applies a timed mark to the target. Reusable by {@code has_mark} filters and the
 * {@code target_has_mark} condition to build detonation/combo mechanics.
 */
public record MarkHandler(Identifier mark, int duration) implements EntityEffectHandler {
	public static final String TYPE = "forgero:mark";

	public static final Codec<MarkHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("mark").forGetter(MarkHandler::mark),
			Codec.INT.fieldOf("duration").forGetter(MarkHandler::duration)
	).apply(instance, MarkHandler::new));

	public MarkHandler {
		if (duration <= 0) {
			throw new IllegalArgumentException("mark duration must be > 0, got: " + duration);
		}
	}

	@Override
	public void apply(Entity entity) {
		if (entity instanceof LivingEntity living && !living.getWorld().isClient()) {
			MarkStore.mark(living, mark, duration);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
