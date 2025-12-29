package com.sigmundgranaas.forgero.bows.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;

/**
 * Handler that increments player statistics when the bow is used.
 *
 * <p>This tracks bow usage in the player's statistics screen, matching vanilla
 * behavior where each bow shot increments the "Times Used" stat for that item.
 *
 * <p>Only increments on the server side to prevent duplicate stat tracking.
 *
 * <p><b>Example JSON:</b>
 * <pre>{@code
 * {
 *   "type": "forgero:increment_stat",
 *   "stat_type": "USED"
 * }
 * }</pre>
 */
public record IncrementStatHandler(
		String statType
) implements ContextualUseHandler {
	public static final String TYPE = "forgero:increment_stat";

	public static final Codec<IncrementStatHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("stat_type", "USED").forGetter(IncrementStatHandler::statType)
			).apply(instance, IncrementStatHandler::new)
	);

	@Override
	public void apply(UseContext context) {
		// Only increment on server side
		if (context.world().isClient()) {
			return;
		}

		// Only players have statistics
		if (!(context.user() instanceof PlayerEntity player)) {
			return;
		}

		// Get the item being used (the bow)
		var stack = player.getActiveItem();
		if (stack.isEmpty()) {
			return;
		}

		// Increment the appropriate stat
		switch (statType.toUpperCase()) {
			case "USED" -> player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
			// Future: Could add more stat types here (BROKEN, CRAFTED, etc.)
			default -> player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
