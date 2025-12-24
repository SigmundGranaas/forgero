package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that applies a cooldown to the item after use.
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:cooldown",
 *   "duration": 20
 * }
 * </pre>
 */
public record CooldownHandler(int duration) implements SimpleUseHandler {

	public static final String TYPE = "forgero:cooldown";

	public static final Codec<CooldownHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.fieldOf("duration").forGetter(CooldownHandler::duration)
			).apply(instance, CooldownHandler::new)
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player) {
			player.getItemCooldownManager().set(stack.getItem(), duration);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
