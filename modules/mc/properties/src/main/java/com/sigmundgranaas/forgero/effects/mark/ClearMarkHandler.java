package com.sigmundgranaas.forgero.effects.mark;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Removes a mark from the target (e.g. to consume it after a detonation).
 */
public record ClearMarkHandler(Identifier mark) implements EntityEffectHandler {
	public static final String TYPE = "forgero:clear_mark";

	public static final Codec<ClearMarkHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("mark").forGetter(ClearMarkHandler::mark)
	).apply(instance, ClearMarkHandler::new));

	@Override
	public void apply(Entity entity) {
		if (entity instanceof LivingEntity living && !living.getWorld().isClient()) {
			MarkStore.clearMark(living, mark);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
