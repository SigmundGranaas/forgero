package com.sigmundgranaas.forgero.loot;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

import static com.sigmundgranaas.forgero.loot.ToolPartFilter.getToolPartValue;

public class TreasureInjector {
    private static final List<ForgeroToolPartTypes> ALL_TOOL_PARTS = List.of(ForgeroToolPartTypes.values());

    public void registerLoot() {
        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, table, setter) -> {
            if (id.equals(LootTables.ABANDONED_MINESHAFT_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(ForgeroToolTypes.PICKAXE)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(10, 50))
                );

                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterToolType(ForgeroToolTypes.PICKAXE)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 50))
                );
            }
            if (id.equals(LootTables.END_CITY_TREASURE_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(50, 100))
                );
                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterLevel(50, 100))
                );
            }
            if (id.equals(LootTables.DESERT_PYRAMID_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(ForgeroToolTypes.SHOVEL)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(20, 60))
                );

                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterToolType(ForgeroToolTypes.SHOVEL)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 50))
                );
            }
            if (id.equals(LootTables.SHIPWRECK_TREASURE_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(ForgeroToolTypes.AXE)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(20, 60))
                );
                //axes, swords, ++, 20-60
            }
            if (id.equals(LootTables.RUINED_PORTAL_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterMaterial("gold"))
                );
                //only gold toolparts
            }
            if (id.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterLevel(30))
                );
                //all tool parts, 0-30
            }
            if (id.equals(LootTables.PILLAGER_OUTPOST_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(List.of(ForgeroToolTypes.AXE))
                                .filterToolPartType(ForgeroToolPartTypes.HANDLE)
                                .filterLevel(30))
                );
                //weapons and handles, ++,0-30
            }
            if (id.equals(LootTables.STRONGHOLD_LIBRARY_CHEST)) {
                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterLevel(40, 80))
                );
                //all patterns, 40-80
            }
            if (id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(List.of(ForgeroToolTypes.AXE))
                                .filterToolPartType(ForgeroToolPartTypes.HANDLE)
                                .filterLevel(20, 80))
                );

                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterToolType(ForgeroToolTypes.AXE)
                                .filterToolType(ForgeroToolTypes.PICKAXE)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(20, 80))
                );
                //weapons and axes and patterns - 20-80
            }
            if (id.equals(LootTables.VILLAGE_WEAPONSMITH_CHEST)) {
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(List.of(ForgeroToolTypes.AXE))
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 60))
                );
                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterToolType(ForgeroToolTypes.AXE)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 40))
                );

                //weapons and axes and patterns - 0-60
            }
            if (id.equals(LootTables.VILLAGE_TOOLSMITH_CHEST)) {
                //TOOLS and patterns - 0-60
                table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
                        ToolPartFilter.createToolPartFilter()
                                .filterToolType(List.of(ForgeroToolTypes.AXE, ForgeroToolTypes.PICKAXE, ForgeroToolTypes.SHOVEL))
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 60))
                );
                table.pool(registerPatternsInPool(createStandardConstantPool(),
                        PatternFilter.createPatternFilter()
                                .filterToolType(ForgeroToolTypes.AXE)
                                .filterToolType(ForgeroToolTypes.PICKAXE)
                                .filterToolType(ForgeroToolTypes.SHOVEL)
                                .filterToolPartType(ALL_TOOL_PARTS)
                                .filterLevel(0, 40))
                );

            }
        });
    }

    private LootPool.Builder registerToolPartsIntoPool(LootPool.Builder pool, ToolPartFilter toolPartFilter) {
        for (ForgeroToolPart toolPart : toolPartFilter.getToolParts()) {
            Item toolPartItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()));
            pool.with(ItemEntry.builder(toolPartItem).weight(1000 - getToolPartValue(toolPart)));
        }
        return pool;
    }

    private LootPool.Builder registerPatternsInPool(LootPool.Builder pool, PatternFilter toolPartFilter) {
        for (Pattern pattern : toolPartFilter.getPatterns()) {
            Item patternItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()));
            pool.with(ItemEntry.builder(patternItem).weight(100 - pattern.getRarity()));
        }
        return pool;
    }

    private LootPool.Builder createStandardConstantPool() {
        return FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1));
    }
}
