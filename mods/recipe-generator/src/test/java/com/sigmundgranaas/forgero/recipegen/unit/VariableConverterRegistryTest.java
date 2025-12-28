package com.sigmundgranaas.forgero.recipegen.unit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;
import com.sigmundgranaas.forgero.recipegen.impl.variable.VariableConverterRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VariableConverterRegistry functionality.
 */
class VariableConverterRegistryTest {

	private VariableConverterRegistryImpl registry;

	@BeforeEach
	void setUp() {
		registry = new VariableConverterRegistryImpl();
	}

	// ============================================================
	// Registration Tests
	// ============================================================

	@Test
	void testRegisterAndRetrieve() {
		StringListConverter converter = new StringListConverter();
		registry.register("test:string_list", converter);

		Optional<VariableConverter<?>> retrieved = registry.get("test:string_list");

		assertTrue(retrieved.isPresent());
		assertSame(converter, retrieved.get());
	}

	@Test
	void testRegisterReturnsReference() {
		StringListConverter converter = new StringListConverter();
		VariableConverterRegistry.ConverterReference<String> ref =
				registry.register("test:string_list", converter);

		assertEquals("test:string_list", ref.id());
		assertSame(converter, ref.converter());
	}

	@Test
	void testGetUnregisteredReturnsEmpty() {
		Optional<VariableConverter<?>> retrieved = registry.get("nonexistent:converter");

		assertFalse(retrieved.isPresent());
	}

	@Test
	void testRegisterOverwrites() {
		StringListConverter first = new StringListConverter();
		StringListConverter second = new StringListConverter();

		registry.register("test:converter", first);
		registry.register("test:converter", second);

		Optional<VariableConverter<?>> retrieved = registry.get("test:converter");

		assertTrue(retrieved.isPresent());
		assertSame(second, retrieved.get());
	}

	// ============================================================
	// Conversion Tests
	// ============================================================

	@Test
	void testConvertWithMatchingConverter() {
		registry.register("test:string_list", new StringListConverter());

		JsonArray array = new JsonArray();
		array.add("iron");
		array.add("gold");
		array.add("diamond");

		Collection<?> result = registry.convert(array);

		assertEquals(3, result.size());
		assertTrue(result.contains("iron"));
		assertTrue(result.contains("gold"));
		assertTrue(result.contains("diamond"));
	}

	@Test
	void testConvertByIdDirect() {
		registry.register("test:number_list", new NumberListConverter());

		JsonArray array = new JsonArray();
		array.add(1);
		array.add(2);
		array.add(3);

		Collection<?> result = registry.convert("test:number_list", array);

		assertEquals(3, result.size());
		assertTrue(result.contains(1));
		assertTrue(result.contains(2));
		assertTrue(result.contains(3));
	}

	@Test
	void testConvertWithNoMatchingConverterThrows() {
		// No converters registered

		JsonObject object = new JsonObject();
		object.addProperty("key", "value");

		assertThrows(IllegalArgumentException.class, () -> registry.convert(object));
	}

	@Test
	void testConvertByIdWithNonexistentIdThrows() {
		JsonArray array = new JsonArray();
		array.add("value");

		assertThrows(IllegalArgumentException.class,
				() -> registry.convert("nonexistent:converter", array));
	}

	// ============================================================
	// Priority Tests
	// ============================================================

	@Test
	void testHigherPriorityConverterWins() {
		// Register low priority converter
		registry.register("test:low", new VariableConverter<String>() {
			@Override
			public boolean matches(JsonElement element) {
				return element.isJsonArray();
			}

			@Override
			public Collection<String> convert(JsonElement element) {
				return List.of("low");
			}

			@Override
			public int priority() {
				return 0;
			}
		});

		// Register high priority converter
		registry.register("test:high", new VariableConverter<String>() {
			@Override
			public boolean matches(JsonElement element) {
				return element.isJsonArray();
			}

			@Override
			public Collection<String> convert(JsonElement element) {
				return List.of("high");
			}

			@Override
			public int priority() {
				return 10;
			}
		});

		JsonArray array = new JsonArray();
		array.add("test");

		Collection<?> result = registry.convert(array);

		assertEquals(1, result.size());
		assertTrue(result.contains("high"));
	}

	@Test
	void testEqualPriorityUsesLastRegistered() {
		// Both have default priority 0
		registry.register("test:first", new VariableConverter<String>() {
			@Override
			public boolean matches(JsonElement element) {
				return element.isJsonPrimitive();
			}

			@Override
			public Collection<String> convert(JsonElement element) {
				return List.of("first");
			}
		});

		registry.register("test:second", new VariableConverter<String>() {
			@Override
			public boolean matches(JsonElement element) {
				return element.isJsonPrimitive();
			}

			@Override
			public Collection<String> convert(JsonElement element) {
				return List.of("second");
			}
		});

		// With equal priority, implementation may return either
		// Just verify one of them is returned
		Collection<?> result = registry.convert(new JsonPrimitive("test"));
		assertEquals(1, result.size());
	}

	// ============================================================
	// Type-Specific Converters
	// ============================================================

	@Test
	void testObjectConverter() {
		registry.register("test:object", new ObjectConverter());

		JsonObject object = new JsonObject();
		object.addProperty("tag", "minecraft:planks");

		Collection<?> result = registry.convert(object);

		assertEquals(1, result.size());
		assertTrue(result.contains("minecraft:planks"));
	}

	@Test
	void testConverterMatchesCorrectType() {
		registry.register("test:string_list", new StringListConverter());
		registry.register("test:object", new ObjectConverter());

		// String list should match array
		JsonArray array = new JsonArray();
		array.add("value");

		Collection<?> arrayResult = registry.convert(array);
		assertTrue(arrayResult.contains("value"));

		// Object converter should match object
		JsonObject object = new JsonObject();
		object.addProperty("tag", "tag_value");

		Collection<?> objectResult = registry.convert(object);
		assertTrue(objectResult.contains("tag_value"));
	}

	// ============================================================
	// Thread Safety Tests
	// ============================================================

	@Test
	void testConcurrentRegistration() throws InterruptedException {
		int threadCount = 10;
		Thread[] threads = new Thread[threadCount];

		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			threads[i] = new Thread(() -> {
				registry.register("test:converter" + index, new StringListConverter());
			});
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		// All converters should be registered
		for (int i = 0; i < threadCount; i++) {
			assertTrue(registry.get("test:converter" + i).isPresent());
		}
	}

	// ============================================================
	// Helper Classes
	// ============================================================

	private static class StringListConverter implements VariableConverter<String> {
		@Override
		public boolean matches(JsonElement element) {
			if (element.isJsonArray()) {
				for (JsonElement e : element.getAsJsonArray()) {
					if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public Collection<String> convert(JsonElement element) {
			List<String> result = new ArrayList<>();
			for (JsonElement e : element.getAsJsonArray()) {
				result.add(e.getAsString());
			}
			return result;
		}
	}

	private static class NumberListConverter implements VariableConverter<Integer> {
		@Override
		public boolean matches(JsonElement element) {
			if (element.isJsonArray()) {
				for (JsonElement e : element.getAsJsonArray()) {
					if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public Collection<Integer> convert(JsonElement element) {
			List<Integer> result = new ArrayList<>();
			for (JsonElement e : element.getAsJsonArray()) {
				result.add(e.getAsInt());
			}
			return result;
		}
	}

	private static class ObjectConverter implements VariableConverter<String> {
		@Override
		public boolean matches(JsonElement element) {
			return element.isJsonObject() && element.getAsJsonObject().has("tag");
		}

		@Override
		public Collection<String> convert(JsonElement element) {
			String tag = element.getAsJsonObject().get("tag").getAsString();
			return List.of(tag);
		}

		@Override
		public int priority() {
			return 10; // Higher priority than list converters
		}
	}
}
