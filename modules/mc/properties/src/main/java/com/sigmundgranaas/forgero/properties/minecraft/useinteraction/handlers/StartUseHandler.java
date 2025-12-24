package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that initiates the use action by setting the player's current hand.
 * This is the essential first step for any use interaction that requires holding.
 *
 * <p>This handler MUST be included in on_start for any item that needs:</p>
 * <ul>
 *   <li>Draw/charge animations (bows, crossbows)</li>
 *   <li>Consumption animations (eating, drinking)</li>
 *   <li>Any use_action that requires holding</li>
 * </ul>
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:start_use"
 * }
 * </pre>
 */
public record StartUseHandler() implements SimpleUseHandler {

	public static final String TYPE = "forgero:start_use";

	public static final Codec<StartUseHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new StartUseHandler())
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player) {
			player.setCurrentHand(hand);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
