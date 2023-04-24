package com.sigmundgranaas.forgero.quilt.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExtendedLootTable {
	public static boolean isValid() {
		return FabricLoader.getInstance().isModLoaded("forgero-extended");
	}

	public static void register() {
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (source.isBuiltin() && id.equals(EntityType.POLAR_BEAR.getLootTableId())) {
				LootPool.Builder poolBuilder = LootPool.builder()
						.with(ItemEntry.builder(Registry.ITEM.get(new Identifier("forgero:polar_bear_pelt"))));
				tableBuilder.pool(poolBuilder);
			}
		});
	}
}
