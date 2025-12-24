package com.sigmundgranaas.forgero.properties.minecraft.entityuse.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.useinteraction.EntityUseEffect;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * Heals the target entity when used on it.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:heal_entity",
 *   "amount": 5.0
 * }
 * </pre>
 */
public record HealEntityEffect(float amount) implements EntityUseEffect {
	public static final String TYPE = "forgero:heal_entity";

	public static final Codec<HealEntityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("amount").forGetter(HealEntityEffect::amount)
	).apply(instance, HealEntityEffect::new));

	@Override
	public ActionResult apply(UseContext context) {
		if (context.isClient()) {
			return ActionResult.SUCCESS;
		}

		return context.getTarget()
				.filter(entity -> entity instanceof LivingEntity)
				.map(entity -> {
					((LivingEntity) entity).heal(amount);
					return ActionResult.SUCCESS;
				})
				.orElse(ActionResult.PASS);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
