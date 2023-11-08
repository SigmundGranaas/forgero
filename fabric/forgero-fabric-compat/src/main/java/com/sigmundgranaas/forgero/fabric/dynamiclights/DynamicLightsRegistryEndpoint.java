package com.sigmundgranaas.forgero.fabric.dynamiclights;

import static dev.lambdaurora.lambdynlights.api.DynamicLightHandlers.registerDynamicLightHandler;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.LumaHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

public class DynamicLightsRegistryEndpoint implements DynamicLightsInitializer {

	@Override
	public void onInitializeDynamicLights() {
		registerDynamicLightHandler(EntityType.PLAYER, (player) -> {
			for (ItemStack stack : player.getHandItems()) {
				Optional<Integer> luma = StateService.INSTANCE.convert(stack).flatMap(LumaHandler::of).map(ComputedAttribute::asInt);
				if (luma.isPresent()) {
					return luma.get();
				}
			}
			return 0;
		});
	}
}
