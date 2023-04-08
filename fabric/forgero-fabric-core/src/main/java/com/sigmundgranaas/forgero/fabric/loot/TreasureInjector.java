package com.sigmundgranaas.forgero.fabric.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sigmundgranaas.forgero.minecraft.common.loot.LootEntry;
import com.sigmundgranaas.forgero.minecraft.common.loot.SingleLootEntry;
import com.sigmundgranaas.forgero.minecraft.common.loot.StateFilter;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

public class TreasureInjector {
	private static TreasureInjector INSTANCE;
	private final Map<String, LootEntry> entryMap;
	private final List<LootEntry> entries;

	public TreasureInjector() {
		this.entryMap = new HashMap<>();
		this.entries = new ArrayList<>();
	}

	public static TreasureInjector getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TreasureInjector();
		}
		return INSTANCE;
	}

	private void registerEntries() {
		this.entries.clear();
		stronghold();
		ancientCity();
		pyramid();
		junglePyramid();
		toolSmithAndPillagerOutPost();
		weaponsSmith();
		ruinedPortal();
		shipwreck();
		endCity();
		netherFortress();
		bastionRemnants();
		spawner();
		witch();
		zombie();
		pillager();

		smallForgingHouse();

	}

	private void registerEntry(LootEntry entry) {
		this.entries.add(entry);
	}

	public void registerEntry(String id, LootEntry entry) {
		this.entryMap.put(id, entry);
	}

	public void registerLoot() {
		registerEntries();
		if (ExtendedLootTable.isValid()) {
			ExtendedLootTable.register();
		}
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, setter) -> {
			entries.stream().filter(entry -> entry.matches(id)).forEach(entry -> entry.apply(table));
		});
	}

	public void registerDynamicLoot() {
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, setter) -> {
			entryMap.values().stream().filter(entry -> entry.matches(id)).forEach(entry -> entry.apply(table));
		});
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


		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
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

		registerEntry(gemEntry);
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

	private void toolSmithAndPillagerOutPost() {
		var partFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(50)
				.types(List.of("AXE_HEAD", "PICKAXE_HEAD", "HOE_HEAD", "SHOVEL_HEAD", "HANDLE", "TOOL_BINDING"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.VILLAGE_TOOLSMITH_CHEST, LootTables.PILLAGER_OUTPOST_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(40)
				.types(List.of("AXE_HEAD_SCHEMATIC", "PICKAXE_HEAD_SCHEMATIC", "HOE_HEAD_SCHEMATIC", "SHOVEL_HEAD_SCHEMATIC", "HANDLE_SCHEMATIC", "TOOL_BINDING_SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.VILLAGE_TOOLSMITH_CHEST, LootTables.PILLAGER_OUTPOST_CHEST))
				.chance(0.3f)
				.rolls(1)
				.build();

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void weaponsSmith() {
		var partFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(50)
				.types(List.of("AXE_HEAD", "SWORD_BLADE", "SWORD_GUARD", "HANDLE"));

		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.VILLAGE_WEAPONSMITH_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(5)
				.upperRarity(40)
				.types(List.of("AXE_HEAD_SCHEMATIC", "SWORD_BLADE_SCHEMATIC", "SWORD_GUARD_SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.VILLAGE_WEAPONSMITH_CHEST))
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

	private void shipwreck() {
		var partFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(80)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.SHIPWRECK_TREASURE_CHEST, LootTables.BURIED_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(LootTables.SHIPWRECK_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);

		var schematicFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(50)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.SHIPWRECK_TREASURE_CHEST, LootTables.BURIED_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(1)
				.build();


		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void netherFortress() {
		var partFilter = StateFilter.builder()
				.lowerRarity(40)
				.upperRarity(70)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.NETHER_BRIDGE_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(20)
				.upperRarity(50)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.NETHER_BRIDGE_CHEST))
				.chance(0.3f)
				.rolls(1)
				.build();

		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.include(List.of("blazing"))
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(LootTables.NETHER_BRIDGE_CHEST))
				.chance(0.1f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void bastionRemnants() {
		var goldFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(100)
				.include(List.of("gold"))
				.types(List.of("PART"));
		var goldPartEntry = SingleLootEntry.builder()
				.filter(goldFilter.build()::filter)
				.target(List.of(LootTables.BASTION_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.include(List.of("blazed", "quartz"))
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(LootTables.BASTION_TREASURE_CHEST))
				.chance(0.1f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);

		var partFilter = StateFilter.builder()
				.lowerRarity(50)
				.upperRarity(90)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.BASTION_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(20)
				.upperRarity(70)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.BASTION_TREASURE_CHEST))
				.chance(0.3f)
				.rolls(1)
				.build();

		registerEntry(goldPartEntry);
		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void endCity() {
		var partFilter = StateFilter.builder()
				.lowerRarity(50)
				.upperRarity(130)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.END_CITY_TREASURE_CHEST))
				.chance(0.5f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(70)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.END_CITY_TREASURE_CHEST))
				.chance(0.5f)
				.rolls(1)
				.build();

		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(LootTables.END_CITY_TREASURE_CHEST))
				.chance(0.5f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void spawner() {
		var partFilter = StateFilter.builder()
				.lowerRarity(20)
				.upperRarity(50)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(LootTables.SIMPLE_DUNGEON_CHEST))
				.chance(0.3f)
				.rolls(2)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(1)
				.upperRarity(40)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(LootTables.SIMPLE_DUNGEON_CHEST))
				.chance(0.3f)
				.rolls(1)
				.build();

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void zombie() {
		var partFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(40)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(EntityType.ZOMBIE.getLootTableId()))
				.chance(0.01f)
				.rolls(1)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(5)
				.upperRarity(40)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(EntityType.ZOMBIE.getLootTableId()))
				.chance(0.01f)
				.rolls(1)
				.build();

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void pillager() {
		var partFilter = StateFilter.builder()
				.lowerRarity(40)
				.upperRarity(50)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(EntityType.PILLAGER.getLootTableId()))
				.chance(0.1f)
				.rolls(1)
				.build();

		var schematicFilter = StateFilter.builder()
				.lowerRarity(1)
				.upperRarity(40)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(EntityType.PILLAGER.getLootTableId()))
				.chance(0.05f)
				.rolls(1)
				.build();

		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}

	private void witch() {
		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(EntityType.WITCH.getLootTableId()))
				.chance(0.01f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);
	}

	private void smallForgingHouse() {

		var id = new Identifier("forgero:chests/treasure_loot");
		var partFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(80)
				.types(List.of("PART"));
		var partEntry = SingleLootEntry.builder()
				.filter(partFilter.build()::filter)
				.target(List.of(id))
				.chance(0.3f)
				.rolls(2)
				.build();

		var gemFilter = StateFilter.builder()
				.lowerRarity(30)
				.upperRarity(60)
				.types(List.of("GEM"));
		var gemEntry = SingleLootEntry.builder()
				.filter(gemFilter.build()::filter)
				.target(List.of(id))
				.chance(0.3f)
				.rolls(2)
				.build();
		registerEntry(gemEntry);

		var schematicFilter = StateFilter.builder()
				.lowerRarity(10)
				.upperRarity(50)
				.types(List.of("SCHEMATIC"));
		var schematicEntry = SingleLootEntry.builder()
				.filter(schematicFilter.build()::filter)
				.target(List.of(id))
				.chance(0.3f)
				.rolls(1)
				.build();


		registerEntry(partEntry);
		registerEntry(schematicEntry);
	}
}
