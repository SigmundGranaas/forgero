package com.sigmundgranaas.forgero.fabric.dynamiclights;

import static dev.lambdaurora.lambdynlights.api.DynamicLightHandlers.registerDynamicLightHandler;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.LumaHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

public class DynamicLightsRegistryEndpoint implements DynamicLightsInitializer {

	@Override
	public void onInitializeDynamicLights() {
		registerDynamicLightHandler(EntityType.PLAYER, (player) -> {
			for (ItemStack stack : player.getHandItems()) {
				Optional<Integer> luma = CachedConverter.of(stack).flatMap(LumaHandler::of).map(Attribute::asInt);
				if (luma.isPresent()) {
					return luma.get();
				}
			}
			return 0;
		});
	}
}
