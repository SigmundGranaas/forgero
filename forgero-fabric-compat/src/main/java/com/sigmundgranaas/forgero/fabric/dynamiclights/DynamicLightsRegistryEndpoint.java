package com.sigmundgranaas.forgero.fabric.dynamiclights;

import com.sigmundgranaas.forgero.core.property.passive.LeveledPassiveType;
import com.sigmundgranaas.forgero.core.property.passive.LeveledProperty;
import com.sigmundgranaas.forgero.core.property.passive.PassivePropertyType;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import static dev.lambdaurora.lambdynlights.api.DynamicLightHandlers.registerDynamicLightHandler;

public class DynamicLightsRegistryEndpoint implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights() {
        registerDynamicLightHandler(EntityType.PLAYER, (player) -> {
            for (ItemStack stack : player.getItemsHand()) {
                if (stack.getItem() instanceof StateItem stateItem) {
                    var luma = stateItem.dynamicState(stack)
                            .stream().getPassiveProperties()
                            .filter(pas -> pas.getPassiveType() == PassivePropertyType.LEVELED)
                            .map(LeveledProperty.class::cast)
                            .filter(pas -> pas.type() == LeveledPassiveType.EMISSIVE)
                            .findFirst();
                    if (luma.isPresent()) {
                        return 10;
                    }
                }
            }
            return 0;
        });
    }
}
