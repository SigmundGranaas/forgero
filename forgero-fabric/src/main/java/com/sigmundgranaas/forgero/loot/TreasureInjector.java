package com.sigmundgranaas.forgero.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootTables;

import java.util.ArrayList;
import java.util.List;

public class TreasureInjector {
    private final List<LootEntry> entries;

    public TreasureInjector() {
        this.entries = new ArrayList<>();
    }


    private void registerEntries() {
        mineshaft();
        stronghold();
        ancientCity();
        pyramid();
        junglePyramid();
        toolSmith();
        ruinedPortal();
    }

    private void registerEntry(LootEntry entry) {
        this.entries.add(entry);
    }

    public void registerLoot() {
        registerEntries();
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, setter) -> {
            entries.stream().filter(entry -> entry.matches(id)).forEach(entry -> entry.apply(table));
        });


    }

    private void mineshaft() {
        var partFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(50)
                .types(List.of("PICKAXE_HEAD", "TOOL_BINDING", "HANDLE"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.ABANDONED_MINESHAFT_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(40)
                .types(List.of("PICKAXE_HEAD_SCHEMATIC", "HANDLE_SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.ABANDONED_MINESHAFT_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void stronghold() {
        var partFilter = StateFilter.builder()
                .lowerRarity(30)
                .upperRarity(90)
                .types(List.of("PART"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.STRONGHOLD_CROSSING_CHEST, LootTables.STRONGHOLD_CROSSING_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(30)
                .upperRarity(90)
                .types(List.of("SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.STRONGHOLD_CROSSING_CHEST, LootTables.STRONGHOLD_CROSSING_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void ancientCity() {
        var partFilter = StateFilter.builder()
                .lowerRarity(60)
                .upperRarity(120)
                .types(List.of("PART"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.ANCIENT_CITY_CHEST, LootTables.ANCIENT_CITY_ICE_BOX_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(60)
                .upperRarity(120)
                .types(List.of("SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.ANCIENT_CITY_CHEST, LootTables.ANCIENT_CITY_ICE_BOX_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void pyramid() {
        var partFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(50)
                .types(List.of("SHOVEL_HEAD", "TOOL_BINDING", "HANDLE"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.DESERT_PYRAMID_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(40)
                .types(List.of("SHOVEL_HEAD_SCHEMATIC", "HANDLE_SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.DESERT_PYRAMID_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void junglePyramid() {
        var partFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(50)
                .types(List.of("AXE_HEAD", "TOOL_BINDING", "HANDLE"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.JUNGLE_TEMPLE_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(40)
                .types(List.of("AXE_HEAD_SCHEMATIC", "HANDLE_SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.JUNGLE_TEMPLE_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void toolSmith() {
        var partFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(50)
                .types(List.of("PART"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.VILLAGE_TOOLSMITH_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        var schematicFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(40)
                .types(List.of("SCHEMATIC"));
        var schematicEntry = SingleLootEntry.builder()
                .filter(schematicFilter.build()::filter)
                .target(List.of(LootTables.VILLAGE_TOOLSMITH_CHEST))
                .chance(0.3f)
                .rolls(1)
                .build();

        registerEntry(partEntry);
        registerEntry(schematicEntry);
    }

    private void ruinedPortal() {
        var partFilter = StateFilter.builder()
                .lowerRarity(10)
                .upperRarity(90)
                .include(List.of("gold"))
                .types(List.of("PART"));
        var partEntry = SingleLootEntry.builder()
                .filter(partFilter.build()::filter)
                .target(List.of(LootTables.RUINED_PORTAL_CHEST))
                .chance(0.3f)
                .rolls(2)
                .build();

        registerEntry(partEntry);
    }

}