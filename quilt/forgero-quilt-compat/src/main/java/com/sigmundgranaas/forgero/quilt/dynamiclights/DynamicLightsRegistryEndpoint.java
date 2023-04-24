package com.sigmundgranaas.forgero.quilt.dynamiclights;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.LumaHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import java.util.Optional;

import static dev.lambdaurora.lambdynlights.api.DynamicLightHandlers.registerDynamicLightHandler;

public class DynamicLightsRegistryEndpoint implements DynamicLightsInitializer {

	@Override
	public void onInitializeDynamicLights() {
		registerDynamicLightHandler(EntityType.PLAYER, (player) -> {
			for (ItemStack stack : player.getHandItems()) {
				Optional<Integer> luma = StateConverter.of(stack).flatMap(LumaHandler::of).map(Attribute::asInt);
				if (luma.isPresent()) {
					return luma.get();
				}
			}
			return 0;
		});
	}
}
