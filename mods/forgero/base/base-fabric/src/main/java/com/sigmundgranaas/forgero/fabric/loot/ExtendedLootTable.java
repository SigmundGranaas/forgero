package com.sigmundgranaas.forgero.fabric.loot;

import com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils;
import com.sigmundgranaas.forgero.core.Forgero;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ExtendedLootTable {
	public static boolean isValid() {
		return ModLoaderUtils.isModPresent("forgero-extended");
	}

	public static void register() {
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (!source.isBuiltin() || !id.equals(EntityType.POLAR_BEAR.getLootTableId())) {
				return;
			}

			LootPool.Builder poolBuilder = LootPool.builder()
			                                       .with(ItemEntry.builder(
					                                       Registries.ITEM.get(new Identifier(Forgero.NAMESPACE, "polar_bear_pelt"))));
			tableBuilder.pool(poolBuilder);
		});

		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			if (!source.isBuiltin() || !id.equals(EntityType.ENDER_DRAGON.getLootTableId())) {
				return;
			}

			LootPool.Builder poolBuilder = LootPool.builder()
			                                       .with(ItemEntry.builder(
					                                       Registries.ITEM.get(new Identifier(Forgero.NAMESPACE, "dragon_scale"))));
			tableBuilder.pool(poolBuilder);
		});
	}
}
