package com.sigmundgranaas.forgero.loot;

public class TreasureInjector {

    public void registerLoot() {
        /**
         LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, setter) -> {
         if (id.equals(LootTables.ABANDONED_MINESHAFT_CHEST)) {
         table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
         ToolPartFilter.createToolPartFilter()
         .filterToolType(ForgeroToolTypes.PICKAXE)
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(10, 50))
         );

         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
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
         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
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

         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
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
         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
         .filterLevel(40, 80))
         );
         //all schematics, 40-80
         }
         if (id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
         table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
         ToolPartFilter.createToolPartFilter()
         .filterToolType(List.of(ForgeroToolTypes.AXE))
         .filterToolPartType(ForgeroToolPartTypes.HANDLE)
         .filterLevel(20, 80))
         );

         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
         .filterToolType(ForgeroToolTypes.AXE)
         .filterToolType(ForgeroToolTypes.PICKAXE)
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(20, 80))
         );
         //weapons and axes and schematics - 20-80
         }
         if (id.equals(LootTables.VILLAGE_WEAPONSMITH_CHEST)) {
         table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
         ToolPartFilter.createToolPartFilter()
         .filterToolType(List.of(ForgeroToolTypes.AXE))
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(0, 60))
         );
         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
         .filterToolType(ForgeroToolTypes.AXE)
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(0, 40))
         );

         //weapons and axes and schematics - 0-60
         }
         if (id.equals(LootTables.VILLAGE_TOOLSMITH_CHEST)) {
         //TOOLS and schematics - 0-60
         table.pool(registerToolPartsIntoPool(createStandardConstantPool(),
         ToolPartFilter.createToolPartFilter()
         .filterToolType(List.of(ForgeroToolTypes.AXE, ForgeroToolTypes.PICKAXE, ForgeroToolTypes.SHOVEL))
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(0, 60))
         );
         table.pool(registerSchematicInPool(createStandardConstantPool(),
         SchematicFilter.createSchematicFilter()
         .filterToolType(ForgeroToolTypes.AXE)
         .filterToolType(ForgeroToolTypes.PICKAXE)
         .filterToolType(ForgeroToolTypes.SHOVEL)
         .filterToolPartType(ALL_TOOL_PARTS)
         .filterLevel(0, 40))
         );

         }
         });
         */
    }
/*
    private LootPool.Builder registerToolPartsIntoPool(LootPool.Builder pool, ToolPartFilter toolPartFilter) {
        for (ForgeroToolPart toolPart : toolPartFilter.getToolParts()) {
            Item toolPartItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()));
            pool.with(ItemEntry.builder(toolPartItem).weight(1000 - getToolPartValue(toolPart)));
        }
        return pool;
    }

    private LootPool.Builder registerSchematicInPool(LootPool.Builder pool, SchematicFilter toolPartFilter) {
        for (Schematic pattern : toolPartFilter.getSchematics()) {
            Item patternItem = Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getSchematicIdentifier()));
            pool.with(ItemEntry.builder(patternItem).weight(100 - pattern.getRarity()));
        }
        return pool;
    }

    private LootPool.Builder createStandardConstantPool() {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));
    }
        */
}
