package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that consumes items from the stack after use.
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:consume_stack",
 *   "count": 1
 * }
 * </pre>
 */
public record ConsumeStackHandler(int count) implements SimpleUseHandler {

	public static final String TYPE = "forgero:consume_stack";

	public static final Codec<ConsumeStackHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.optionalFieldOf("count", 1).forGetter(ConsumeStackHandler::count)
			).apply(instance, ConsumeStackHandler::new)
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
			stack.decrement(count);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
