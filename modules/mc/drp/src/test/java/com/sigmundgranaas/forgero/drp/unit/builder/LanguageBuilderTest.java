package com.sigmundgranaas.forgero.drp.unit.builder;

import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LanguageBuilder.
 */
class LanguageBuilderTest {

	@Test
	void testAddItem() {
		LanguageBuilder builder = LanguageBuilder.create()
				.item(new Identifier("forgero", "iron_pickaxe"), "Iron Pickaxe");

		Map<String, String> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertEquals("Iron Pickaxe", entries.get("item.forgero.iron_pickaxe"));
	}

	@Test
	void testAddItemWithString() {
		LanguageBuilder builder = LanguageBuilder.create()
				.item("forgero:diamond_sword", "Diamond Sword");

		assertEquals("Diamond Sword", builder.getEntries().get("item.forgero.diamond_sword"));
	}

	@Test
	void testAddBlock() {
		LanguageBuilder builder = LanguageBuilder.create()
				.block(new Identifier("forgero", "assembly_station"), "Assembly Station");

		assertEquals("Assembly Station", builder.getEntries().get("block.forgero.assembly_station"));
	}

	@Test
	void testAddEntity() {
		LanguageBuilder builder = LanguageBuilder.create()
				.entity(new Identifier("minecraft", "pig"), "Pig");

		assertEquals("Pig", builder.getEntries().get("entity.minecraft.pig"));
	}

	@Test
	void testAddTooltip() {
		LanguageBuilder builder = LanguageBuilder.create()
				.tooltip("forgero.tooltip.durability", "Durability: %s");

		assertEquals("Durability: %s", builder.getEntries().get("forgero.tooltip.durability"));
	}

	@Test
	void testAddGeneric() {
		LanguageBuilder builder = LanguageBuilder.create()
				.add("custom.key", "Custom Value");

		assertEquals("Custom Value", builder.getEntries().get("custom.key"));
	}

	@Test
	void testMerge() {
		LanguageBuilder base = LanguageBuilder.create()
				.item("forgero:item1", "Item 1")
				.item("forgero:item2", "Item 2");

		LanguageBuilder extension = LanguageBuilder.create()
				.item("forgero:item3", "Item 3")
				.item("forgero:item4", "Item 4");

		LanguageBuilder merged = LanguageBuilder.create()
				.merge(base)
				.merge(extension);

		assertEquals(4, merged.getEntries().size());
	}

	@Test
	void testMergeOverwrites() {
		LanguageBuilder base = LanguageBuilder.create()
				.item("forgero:item", "Original");

		LanguageBuilder extension = LanguageBuilder.create()
				.item("forgero:item", "Overwritten");

		LanguageBuilder merged = LanguageBuilder.create()
				.merge(base)
				.merge(extension);

		assertEquals("Overwritten", merged.getEntries().get("item.forgero.item"));
	}

	@Test
	void testFluentChaining() {
		LanguageBuilder builder = LanguageBuilder.create()
				.item("mod:item1", "Item 1")
				.block("mod:block1", "Block 1")
				.entity("mod:entity1", "Entity 1")
				.tooltip("mod.tooltip", "Tooltip")
				.add("mod.custom", "Custom");

		assertEquals(5, builder.getEntries().size());
	}

	@Test
	void testEntriesAreImmutable() {
		LanguageBuilder builder = LanguageBuilder.create()
				.item("mod:item", "Item");

		Map<String, String> entries = builder.getEntries();
		assertThrows(UnsupportedOperationException.class, () ->
				entries.put("new.key", "value"));
	}

	@Test
	void testItemWithNestedPath() {
		LanguageBuilder builder = LanguageBuilder.create()
				.item(new Identifier("forgero", "tools/pickaxe"), "Pickaxe Tool");

		assertEquals("Pickaxe Tool", builder.getEntries().get("item.forgero.tools.pickaxe"));
	}
}
