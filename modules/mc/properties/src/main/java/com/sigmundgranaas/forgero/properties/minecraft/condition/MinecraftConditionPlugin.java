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
		OpenIdentifier damagePercentageType = new OpenIdentifier("forgero", "damage_percentage");
		OpenIdentifier weatherType = new OpenIdentifier("forgero", "weather");
		OpenIdentifier randomType = new OpenIdentifier("forgero", "random");

		context.registerDynamicConditionCodec(
				damagePercentageType.toString(),
				DamagePercentageCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				weatherType.toString(),
				WeatherCondition.CODEC
		);

		context.registerDynamicConditionCodec(
				randomType.toString(),
				RandomCondition.CODEC
		);
	}

	@Override
	public String getId() {
		return "forgero:minecraft-conditions";
	}
}
