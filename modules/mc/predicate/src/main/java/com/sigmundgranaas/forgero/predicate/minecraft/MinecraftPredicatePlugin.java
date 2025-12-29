package com.sigmundgranaas.forgero.predicate.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.DamagePredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.RandomPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.WeatherPredicate;

/**
 * A Forgero data plugin that registers all Minecraft-specific dynamic predicates.
 * <p>
 * This is the single source of truth for all Minecraft predicate registrations.
 * These predicates are evaluated at runtime based on the game context during the "apply" phase.
 * <p>
 * Registered predicates:
 * <ul>
 *   <li>{@code minecraft:entity} - Composite entity predicate (type, flags, stats, equipment, effects, location)</li>
 *   <li>{@code minecraft:block} - Composite block predicate (block type, state properties, location)</li>
 *   <li>{@code minecraft:weather} - Weather condition (rain, thunder)</li>
 *   <li>{@code forgero:damage} - Item damage percentage threshold</li>
 *   <li>{@code forgero:random} - Random chance with optional seeding</li>
 * </ul>
 */
public class MinecraftPredicatePlugin implements DataPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftPredicatePlugin.class);
	public static final String ID = "forgero:minecraft-predicates";

	@Override
	public void register(PluginRegistrationContext context) {
		// Composite predicates
		context.registerDynamicConditionCodec(EntityPredicate.TYPE.toString(), EntityPredicate.CODEC);
		context.registerDynamicConditionCodec(BlockPredicate.TYPE.toString(), BlockPredicate.CODEC);

		// Standalone predicates
		context.registerDynamicConditionCodec(WeatherPredicate.TYPE.toString(), WeatherPredicate.CODEC);
		context.registerDynamicConditionCodec(DamagePredicate.TYPE.toString(), DamagePredicate.CODEC);
		context.registerDynamicConditionCodec(RandomPredicate.TYPE.toString(), RandomPredicate.CODEC);
	}

	@Override
	public String getId() {
		return ID;
	}
}
