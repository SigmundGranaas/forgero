package com.sigmundgranaas.forgero.bows.handlers;

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

	public static final String TYPE = "forgero:mount_projectile";

	public static final Codec<MountProjectileHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new MountProjectileHandler())
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player) {
			boolean hasProjectile = !player.getProjectileType(stack).isEmpty();
			if (!player.getAbilities().creativeMode && !hasProjectile) {
				return;
			}
			player.setCurrentHand(hand);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
