package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.tooltip.value.PlaceholderRegistry;
import com.sigmundgranaas.forgero.common.tooltip.value.TooltipValueResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TooltipValueResolverTest {

	@BeforeEach
	void setUp() {
		PlaceholderRegistry.clear();
	}

	@Test
	void extractPlaceholdersFindsSimplePlaceholder() {
		String template = "Deals {damage} damage";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(1, placeholders.size());
		assertEquals("damage", placeholders.get(0));
	}

	@Test
	void extractPlaceholdersFindsMultiplePlaceholders() {
		String template = "Deals {damage} damage with {speed} speed";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(2, placeholders.size());
		assertEquals("damage", placeholders.get(0));
		assertEquals("speed", placeholders.get(1));
	}

	@Test
	void extractPlaceholdersHandlesFormatSpecifiers() {
		String template = "Value: {damage:%.2f}";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(1, placeholders.size());
		assertEquals("damage", placeholders.get(0));
	}

	@Test
	void extractPlaceholdersHandlesUnderscores() {
		String template = "{attack_damage} and {mining_speed}";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(2, placeholders.size());
		assertEquals("attack_damage", placeholders.get(0));
		assertEquals("mining_speed", placeholders.get(1));
	}

	@Test
	void extractPlaceholdersHandlesNumbers() {
		String template = "{modifier1} and {level2}";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(2, placeholders.size());
		assertEquals("modifier1", placeholders.get(0));
		assertEquals("level2", placeholders.get(1));
	}

	@Test
	void extractPlaceholdersReturnsEmptyForNoPlaceholders() {
		String template = "Just plain text";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertTrue(placeholders.isEmpty());
	}

	@Test
	void extractPlaceholdersReturnsEmptyForNull() {
		List<String> placeholders = TooltipValueResolver.extractPlaceholders(null);

		assertTrue(placeholders.isEmpty());
	}

	@Test
	void extractPlaceholdersReturnsEmptyForEmpty() {
		List<String> placeholders = TooltipValueResolver.extractPlaceholders("");

		assertTrue(placeholders.isEmpty());
	}

	@Test
	void extractPlaceholdersIgnoresInvalidPlaceholders() {
		// Placeholders starting with numbers are invalid
		String template = "{123invalid} but {valid_one} works";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(1, placeholders.size());
		assertEquals("valid_one", placeholders.get(0));
	}

	@Test
	void extractPlaceholdersHandlesComplexFormats() {
		String template = "{value:%+.1f} and {percent:%.0f%%}";

		List<String> placeholders = TooltipValueResolver.extractPlaceholders(template);

		assertEquals(2, placeholders.size());
		assertEquals("value", placeholders.get(0));
		assertEquals("percent", placeholders.get(1));
	}

	@Test
	void placeholderRegistryRegisterWorks() {
		assertFalse(PlaceholderRegistry.isRegistered("custom"));

		PlaceholderRegistry.register("custom", comp -> java.util.Optional.of("value"));

		assertTrue(PlaceholderRegistry.isRegistered("custom"));
	}

	@Test
	void placeholderRegistryUnregisterWorks() {
		PlaceholderRegistry.register("temp", comp -> java.util.Optional.of("value"));
		assertTrue(PlaceholderRegistry.isRegistered("temp"));

		boolean removed = PlaceholderRegistry.unregister("temp");

		assertTrue(removed);
		assertFalse(PlaceholderRegistry.isRegistered("temp"));
	}

	@Test
	void placeholderRegistryCaseInsensitive() {
		PlaceholderRegistry.register("MyPlaceholder", comp -> java.util.Optional.of("value"));

		assertTrue(PlaceholderRegistry.isRegistered("myplaceholder"));
		assertTrue(PlaceholderRegistry.isRegistered("MYPLACEHOLDER"));
		assertTrue(PlaceholderRegistry.isRegistered("MyPlaceholder"));
	}

	@Test
	void placeholderRegistryDefaultsRegistered() {
		// After clear, defaults should be re-registered
		assertTrue(PlaceholderRegistry.isRegistered("material_name"));
		assertTrue(PlaceholderRegistry.isRegistered("namespace"));
		assertTrue(PlaceholderRegistry.isRegistered("full_id"));
		assertTrue(PlaceholderRegistry.isRegistered("display_name"));
	}
}
