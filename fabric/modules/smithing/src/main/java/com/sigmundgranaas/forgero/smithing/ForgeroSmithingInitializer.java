package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.ModItemGroups;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.networking.ModServerMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureHandler;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_SMITHING_KEY =
			RegistryKey.of(
					RegistryKeys.ITEM_GROUP,
					new Identifier(Forgero.NAMESPACE, "smithing")
			);

	@Override
	public void onPreInitialization() {
		ModBlockEntities.registerBlockEntities();

		ModItems.registerModItems();

		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.SMITHING_GROUP_KEY)
				.register(ModItems::addItemsToSmithingGroup);

		TemperatureHandler.register();

		ModServerMessages.registerC2SPackets();
	}
}
