package com.sigmundgranaas.forgero.fabric.loot;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;

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
			if (source.isBuiltin() && id.equals(EntityType.ENDER_DRAGON.getLootTableId())) {
				LootPool.Builder poolBuilder = LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(3))
						.with(ItemEntry.builder(Registry.ITEM.get(new Identifier("forgero:ender_dragon_scale"))));
				tableBuilder.pool(poolBuilder);
			}
			if (source.isBuiltin() && id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
				LootPool.Builder poolBuilder = LootPool.builder()
						.rolls(BinomialLootNumberProvider.create(1, 0.3f))
						.with(ItemEntry.builder(Registry.ITEM.get(new Identifier("forgero:soul-totem"))));
				tableBuilder.pool(poolBuilder);
			}
		});
	}
}
