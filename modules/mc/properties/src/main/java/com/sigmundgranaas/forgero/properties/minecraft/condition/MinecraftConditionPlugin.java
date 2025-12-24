package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

/**
 * Plugin for registering Minecraft-specific dynamic condition codecs.
 * Dynamic conditions are evaluated at runtime based on game state.
 */
public class MinecraftConditionPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		// Register dynamic conditions (minecraft predicates)
		registerDynamicConditions(context);
	}

	private void registerDynamicConditions(PluginRegistrationContext context) {
		// Runtime state conditions
		context.registerDynamicConditionCodec(
				"forgero:damage_percentage",
				DamagePercentageCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:weather",
				WeatherCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:random",
				RandomCondition.CODEC
		);

		// Entity conditions
		context.registerDynamicConditionCodec(
				"forgero:entity_type",
				EntityTypeCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:entity_flag",
				EntityFlagCondition.CODEC
		);

		// Block conditions
		context.registerDynamicConditionCodec(
				"forgero:block_match",
				BlockMatchCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:block_tag",
				BlockTagCondition.CODEC
		);

		// World/Location conditions
		context.registerDynamicConditionCodec(
				"forgero:dimension",
				DimensionCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:biome",
				BiomeCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				"forgero:position",
				PositionCondition.CODEC
		);
	}

	@Override
	public String getId() {
		return "forgero:minecraft-conditions";
	}
}
