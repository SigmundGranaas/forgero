package com.sigmundgranaas.forgero.blocks;

import com.sigmundgranaas.forgero.blocks.block.ModBlocks;
import com.sigmundgranaas.forgero.blocks.item.ModItemGroups;
import com.sigmundgranaas.forgero.blocks.item.ModItems;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;


public class ForgeroBlocksInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_BLOCKS_KEY =  RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "blocks"));

	@Override
	public void onPreInitialization() {
			ModItemGroups.registerItemGroups();
			ModItems.registerModItems();
			ModBlocks.registerModBlocks();
	}


}

