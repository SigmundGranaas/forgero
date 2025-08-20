package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator;
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.ModItemGroups;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.tooltip.CrucibleTooltipComponent;
import com.sigmundgranaas.forgero.smithing.item.tooltip.CrucibleTooltipData;
import com.sigmundgranaas.forgero.smithing.recipe.ModRecipes;
import com.sigmundgranaas.forgero.smithing.resource.MoldGenerator;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureHandler;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;


public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_SMITHING_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "smithing"));

	@Override
	public void onPreInitialization() {
		ModBlockEntities.registerBlockEntities();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.SMITHING_GROUP_KEY)
				.register(ModItems::addItemsToSmithingGroup);
		ModRecipes.registerRecipes();

		ARRPGenerator.register(new MoldGenerator());

		TemperatureHandler.register();

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof CrucibleTooltipData crucibleData) {
				return new CrucibleTooltipComponent(crucibleData);
			}
			return null;
		});
	}
}
