package com.sigmundgranaas.forgero.smithingrework;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;

import com.sigmundgranaas.forgero.smithingrework.block.ModBlocks;
import com.sigmundgranaas.forgero.smithingrework.item.ModItemGroups;

import com.sigmundgranaas.forgero.smithingrework.item.ModItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;


public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_SMITHING_KEY =  RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "smithing"));

	@Override
	public void onPreInitialization() {

			ModItemGroups.registerItemGroups();

			ModItems.registerModItems();
			ModBlocks.registerModBlocks();
	}


}

