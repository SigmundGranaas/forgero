package com.sigmundgranaas.forgero.bows.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that consumes (decrements) projectiles from the player's inventory.
 * Respects Infinity enchantment and creative mode.
 *
 * <p>This handler is typically used in the "on_release" phase of a bow-like item
 * to consume arrows after launching a projectile.</p>
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:consume_projectile"
 * }
 * </pre>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Does nothing in creative mode</li>
 *   <li>Does nothing if bow has Infinity enchantment</li>
 *   <li>Otherwise, decrements the projectile stack by 1</li>
 *   <li>Removes empty stacks from inventory</li>
 * </ul>
 */
public record ConsumeProjectileHandler() implements SimpleUseHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeProjectileHandler.class);
	public static final String TYPE = "forgero:consume_projectile";

	public static final Codec<ConsumeProjectileHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new ConsumeProjectileHandler())
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (!(user instanceof PlayerEntity player)) {
			return;
		}

		// Creative mode doesn't consume arrows
		if (player.getAbilities().creativeMode) {
			LOGGER.trace("Skipping arrow consumption for player {} in creative mode", player.getName().getString());
			return;
		}

		// Infinity enchantment doesn't consume arrows
		if (hasInfinity(stack)) {
			LOGGER.trace("Skipping arrow consumption for player {} with Infinity enchantment", player.getName().getString());
			return;
		}

		// Find and consume the projectile
		ItemStack arrowStack = player.getProjectileType(stack);
		if (!arrowStack.isEmpty()) {
			arrowStack.decrement(1);
			LOGGER.debug("Consumed arrow for player {}, remaining: {}", player.getName().getString(), arrowStack.getCount());
			if (arrowStack.isEmpty()) {
				player.getInventory().removeOne(arrowStack);
			}
		}
	}

	private boolean hasInfinity(ItemStack stack) {
		return EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
