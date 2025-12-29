package com.sigmundgranaas.forgero.bows.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that initiates bow use by checking for available projectiles
 * and setting the player's current hand.
 *
 * <p>This is typically used in the "on_start" phase of UseInteractionProperty.</p>
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:mount_projectile"
 * }
 * </pre>
 */
public record MountProjectileHandler() implements SimpleUseHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MountProjectileHandler.class);
	public static final String TYPE = "forgero:mount_projectile";

	public static final Codec<MountProjectileHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new MountProjectileHandler())
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player) {
			boolean hasProjectile = !player.getProjectileType(stack).isEmpty();
			if (!player.getAbilities().creativeMode && !hasProjectile) {
				LOGGER.debug("Player {} cannot mount projectile: no projectile available and not in creative mode",
						player.getName().getString());
				return;
			}
			player.setCurrentHand(hand);
			LOGGER.trace("Player {} mounted bow in hand {}", player.getName().getString(), hand);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
