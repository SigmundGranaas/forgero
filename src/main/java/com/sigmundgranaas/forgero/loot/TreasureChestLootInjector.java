package com.sigmundgranaas.forgero.loot;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.stream.Stream;

public class TreasureChestLootInjector {
    public static void registerLootTable() {

        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, table, setter) -> {
            if (id.equals(LootTables.SHIPWRECK_TREASURE_CHEST) || id.equals(LootTables.SHIPWRECK_SUPPLY_CHEST) || id.equals(LootTables.SHIPWRECK_MAP_CHEST)) {
                //registerToolPartLoot(table, 1, 1);
                FabricLootPoolBuilder patternPoolBuilder = FabricLootPoolBuilder.builder()
                        .rolls(ConstantLootNumberProvider.create(1));

                FabricLootPoolBuilder toolpartPool = FabricLootPoolBuilder.builder()
                        .rolls(ConstantLootNumberProvider.create(1));

                for (ForgeroToolPart toolPart : ForgeroRegistry.getInstance().toolPartCollection().getToolParts()) {
                    Item toolPartItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()));
                    toolpartPool.with(ItemEntry.builder(toolPartItem).weight(1));
                }

                for (Pattern pattern : ForgeroRegistry.getInstance().patternCollection().getPatterns()) {
                    Item patternItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()));
                    patternPoolBuilder.with(ItemEntry.builder(patternItem));
                }
                table.pool(patternPoolBuilder);
                table.pool(toolpartPool);
            }
            if (isVillageChest(id)) {
                for (Pattern pattern : ForgeroRegistry.getInstance().patternCollection().getPatterns()) {
                    Item patternItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()));
                    FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                            .rolls(ConstantLootNumberProvider.create(1))
                            .with(ItemEntry.builder(patternItem));
                    table.pool(poolBuilder);
                }
                //registerToolPartLoot(table, 1, 1);
            }
        });
    }

    private static boolean isVillageChest(Identifier id) {
        //noinspection SimplifyStreamApiCallChains
        return Stream
                .of(LootTables.VILLAGE_ARMORER_CHEST, LootTables.VILLAGE_TOOLSMITH_CHEST, LootTables.VILLAGE_WEAPONSMITH_CHEST)
                .anyMatch((ids) -> ids.equals(id));
    }

    private static void registerToolPartLoot(FabricLootSupplierBuilder table, float amount, int weight) {
        for (ForgeroToolPart toolPart : ForgeroRegistry.getInstance().toolPartCollection().getToolParts()) {
            Item toolPartItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()));
            FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootNumberProvider.create(amount))
                    .with(ItemEntry.builder(toolPartItem).weight(weight));

            table.pool(poolBuilder);
        }
    }
}
