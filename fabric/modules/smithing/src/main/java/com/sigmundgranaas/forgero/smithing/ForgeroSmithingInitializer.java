package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.ModItemGroups;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.networking.ModServerMessages;
import com.sigmundgranaas.forgero.smithing.particle.ModParticles;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureHandler;

public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		ModParticles.register();

		ModBlockEntities.registerBlockEntities();

		TemperatureHandler.register();

		ModServerMessages.registerC2SPackets();
	}
}
