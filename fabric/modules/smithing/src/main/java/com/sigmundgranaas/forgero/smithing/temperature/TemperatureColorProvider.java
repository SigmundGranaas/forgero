package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class TemperatureColorProvider {
	public static void register() {
		Registries.ITEM.forEach(item -> {
			ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
				if (!TemperatureRules.canTrackTemperature(stack)) {
					return 0xFFFFFF;
				}

				return TemperatureRules.color(stack);
			}, item);
		});
	}
}
