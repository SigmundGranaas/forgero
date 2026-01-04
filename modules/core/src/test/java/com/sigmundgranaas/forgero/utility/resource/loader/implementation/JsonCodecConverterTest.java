package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonCodecConverterTest extends ForgeroTest {

	// Simple test record for codec testing
	record TestData(String name, int value) {
		public static final Codec<TestData> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("name").forGetter(TestData::name),
						Codec.INT.fieldOf("value").forGetter(TestData::value)
				).apply(instance, TestData::new)
		);
	}

	// Complex nested record
	record NestedData(String type, TestData data) {
		public static final Codec<NestedData> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("type").forGetter(NestedData::type),
						TestData.CODEC.fieldOf("data").forGetter(NestedData::data)
				).apply(instance, NestedData::new)
		);
	}

	private final JsonCodecConverter<TestData> simpleConverter = new JsonCodecConverter<>(TestData.CODEC);
	private final JsonCodecConverter<NestedData> nestedConverter = new JsonCodecConverter<>(NestedData.CODEC);

	@Test
	void parsesValidJson() {
		String json = "{\"name\": \"test_item\", \"value\": 42}";
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "test");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertTrue(result.isPresent(), "Should successfully parse valid JSON");
		assertEquals("test_item", result.get().name());
		assertEquals(42, result.get().value());
	}

	@Test
	void parsesNestedStructures() {
		String json = "{\"type\": \"complex\", \"data\": {\"name\": \"nested\", \"value\": 100}}";
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "nested_test");

		Optional<NestedData> result = nestedConverter.convert(stream, id);

		assertTrue(result.isPresent(), "Should successfully parse nested JSON");
		assertEquals("complex", result.get().type());
		assertEquals("nested", result.get().data().name());
		assertEquals(100, result.get().data().value());
	}

	@Test
	void returnsEmptyForInvalidJson() {
		String invalidJson = "{\"name\": \"test\", \"value\": }"; // Missing value after colon
		InputStream stream = toInputStream(invalidJson);
		OpenIdentifier id = new OpenIdentifier("forgero", "invalid");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertFalse(result.isPresent(), "Should return empty Optional for invalid JSON syntax");
	}

	@Test
	void returnsEmptyForMissingRequiredFields() {
		String json = "{\"name\": \"test_only\"}"; // Missing "value" field
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "incomplete");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertFalse(result.isPresent(), "Should return empty Optional when required fields are missing");
	}

	@Test
	void returnsEmptyForTypeMismatch() {
		String json = "{\"name\": \"test\", \"value\": \"not_a_number\"}"; // value should be int
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "type_error");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertFalse(result.isPresent(), "Should return empty Optional for type mismatch");
	}

	@Test
	void handlesEmptyJson() {
		String json = "{}";
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "empty");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertFalse(result.isPresent(), "Should return empty Optional for empty JSON object");
	}

	@Test
	void handlesExtraFields() {
		String json = "{\"name\": \"test\", \"value\": 42, \"extra_field\": \"ignored\"}";
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "extra_fields");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertTrue(result.isPresent(), "Should successfully parse JSON with extra fields (they are ignored)");
		assertEquals("test", result.get().name());
		assertEquals(42, result.get().value());
	}

	@Test
	void handlesUnicodeCharacters() {
		String json = "{\"name\": \"测试\", \"value\": 99}"; // Chinese characters
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "unicode");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertTrue(result.isPresent(), "Should handle Unicode characters correctly");
		assertEquals("测试", result.get().name());
		assertEquals(99, result.get().value());
	}

	@Test
	void handlesLargeNumbers() {
		String json = "{\"name\": \"large\", \"value\": 2147483647}"; // Integer.MAX_VALUE
		InputStream stream = toInputStream(json);
		OpenIdentifier id = new OpenIdentifier("forgero", "large_number");

		Optional<TestData> result = simpleConverter.convert(stream, id);

		assertTrue(result.isPresent(), "Should handle large numbers within int range");
		assertEquals(Integer.MAX_VALUE, result.get().value());
	}

	/**
	 * Helper method to convert a string to an InputStream.
	 */
	private InputStream toInputStream(String str) {
		return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	}
}
