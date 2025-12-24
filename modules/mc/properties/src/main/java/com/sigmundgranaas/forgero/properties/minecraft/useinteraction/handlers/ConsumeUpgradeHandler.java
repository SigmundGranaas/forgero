package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that consumes (removes) specific upgrades from Forgero composite items.
 * This is useful for creating consumable gem sockets or one-time-use enchantments.
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:consume_upgrade",
 *   "identifier": "forgero:magic_gem"
 * }
 * </pre>
 *
 * <h3>Notes:</h3>
 * <p>This handler works with Forgero's component system to remove upgrades.
 * The upgrade must be removable according to the component's rules.</p>
 */
public record ConsumeUpgradeHandler(String identifier) implements SimpleUseHandler {

	public static final String TYPE = "forgero:consume_upgrade";
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeUpgradeHandler.class);

	public static final Codec<ConsumeUpgradeHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("identifier").forGetter(ConsumeUpgradeHandler::identifier)
			).apply(instance, ConsumeUpgradeHandler::new)
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player && player.getAbilities().creativeMode) {
			return; // Don't consume in creative mode
		}

		// TODO: Implement upgrade consumption when component mutation API is available
		// This requires:
		// 1. Finding the upgrade in the component tree
		// 2. Removing it from the tree
		// 3. Converting the modified component back to an ItemStack
		LOGGER.debug("ConsumeUpgradeHandler: upgrade consumption not yet implemented for '{}'", identifier);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
