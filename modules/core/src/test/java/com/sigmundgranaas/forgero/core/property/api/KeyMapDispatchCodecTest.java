package com.sigmundgranaas.forgero.core.property.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyMapDispatchCodecTest {

	private KeyMapDispatchCodec codec;
	private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	// Helper to pretty-print JsonElement
	private String toJson(JsonElement element) {
		return GSON.toJson(element);
	}

	/**
	 * Creates the dispatch map used by KeyMapDispatchCodec, mapping PropertyKeys
	 * to their respective ListCodecWrapper instances.
	 */
	private Map<PropertyKey<?>, Codec<? extends List<?>>> createDispatchMap() {
		Map<PropertyKey<?>,Codec<? extends List<?>>> map = new HashMap<>();
		// For each property type, wrap its CODEC with ListCodecWrapper
		map.put(A_KEY, ListCodecWrapper.of(TestPropertyA.CODEC));
		map.put(B_KEY, ListCodecWrapper.of(TestPropertyB.CODEC));
		map.put(C_KEY, ListCodecWrapper.of(TestPropertyC.CODEC));
		return map;
	}

	@BeforeEach
	void setUp() {
		// Initialize the KeyMapDispatchCodec with the prepared dispatch map
		codec = new KeyMapDispatchCodec(createDispatchMap());
	}

	// --- KeyMapDispatchCodec Tests ---

	@Test
	void testEncodeDecodeBasicSingleValue() {
		Map<String, List<?>> originalMap = new HashMap<>();
		originalMap.put(A_KEY.key(), List.of(new TestPropertyA(10)));
		originalMap.put(B_KEY.key(), List.of(new TestPropertyB("hello")));
		originalMap.put(C_KEY.key(), List.of(new TestPropertyC(true)));

		DataResult<JsonElement> encodedResult = codec.codec().encodeStart(JsonOps.INSTANCE, originalMap);
		assertTrue(encodedResult.result().isPresent(), "Encoding should succeed: " + encodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("Encoded JSON (Basic Single Value): " + toJson(encodedJson));

		// Verify that single-element lists are encoded as single JSON objects
		assertTrue(encodedJson.getAsJsonObject().get(A_KEY.key()).isJsonObject());
		assertTrue(encodedJson.getAsJsonObject().get(B_KEY.key()).isJsonObject());
		assertTrue(encodedJson.getAsJsonObject().get(C_KEY.key()).isJsonObject());

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, encodedJson).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding should succeed: " + decodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		Map<String, List<?>> decodedMap = decodedResult.result().get();

		assertEquals(originalMap.size(), decodedMap.size(), "Decoded map size should match original");
		assertEquals(originalMap.get(A_KEY.key()), decodedMap.get(A_KEY.key()), "PropertyA should match");
		assertEquals(originalMap.get(B_KEY.key()), decodedMap.get(B_KEY.key()), "PropertyB should match");
		assertEquals(originalMap.get(C_KEY.key()), decodedMap.get(C_KEY.key()), "PropertyC should match");
	}

	@Test
	void testEncodeDecodeMultiValue() {
		Map<String, List<?>> originalMap = new HashMap<>();
		originalMap.put(A_KEY.key(), List.of(new TestPropertyA(1), new TestPropertyA(2)));
		originalMap.put(B_KEY.key(), List.of(new TestPropertyB("one"), new TestPropertyB("two")));

		DataResult<JsonElement> encodedResult = codec.codec().encodeStart(JsonOps.INSTANCE, originalMap);
		assertTrue(encodedResult.result().isPresent(), "Encoding should succeed: " + encodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("Encoded JSON (Multi-Value): " + toJson(encodedJson));

		// Verify JSON structure for multi-value (should be an array)
		assertTrue(encodedJson.getAsJsonObject().get(A_KEY.key()).isJsonArray(), "PropertyA should be an array");
		assertTrue(encodedJson.getAsJsonObject().get(B_KEY.key()).isJsonArray(), "PropertyB should be an array");

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, encodedJson).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding should succeed: " + decodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		Map<String, List<?>> decodedMap = decodedResult.result().get();

		assertEquals(originalMap.size(), decodedMap.size(), "Decoded map size should match original");
		assertEquals(originalMap.get(A_KEY.key()), decodedMap.get(A_KEY.key()), "PropertyA should match");
		assertEquals(originalMap.get(B_KEY.key()), decodedMap.get(B_KEY.key()), "PropertyB should match");
	}

	@Test
	void testEncodeDecodeMixedSingleAndMultiValue() {
		Map<String, List<?>> originalMap = new HashMap<>();
		originalMap.put(A_KEY.key(), List.of(new TestPropertyA(100))); // Single value
		originalMap.put(B_KEY.key(), List.of(new TestPropertyB("mixed1"), new TestPropertyB("mixed2"))); // Multi value

		DataResult<JsonElement> encodedResult = codec.codec().encodeStart(JsonOps.INSTANCE, originalMap);
		assertTrue(encodedResult.result().isPresent(), "Encoding should succeed: " + encodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("Encoded JSON (Mixed): " + toJson(encodedJson));

		// Verify JSON structure: A should be single value, B should be array
		assertFalse(encodedJson.getAsJsonObject().get(A_KEY.key()).isJsonArray(), "PropertyA should be a single object");
		assertTrue(encodedJson.getAsJsonObject().get(B_KEY.key()).isJsonArray(), "PropertyB should be an array");

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, encodedJson).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding should succeed: " + decodedResult.error().map(DataResult.PartialResult::message).orElse(""));
		Map<String, List<?>> decodedMap = decodedResult.result().get();

		assertEquals(originalMap.size(), decodedMap.size(), "Decoded map size should match original");
		assertEquals(originalMap.get(A_KEY.key()), decodedMap.get(A_KEY.key()), "PropertyA should match");
		assertEquals(originalMap.get(B_KEY.key()), decodedMap.get(B_KEY.key()), "PropertyB should match");
	}

	@Test
	void testEncodeSkipUnknownKey() {
		Map<String, List<?>> originalMap = new HashMap<>();
		originalMap.put(A_KEY.key(), List.of(new TestPropertyA(10)));
		originalMap.put("unknown_property", List.of("some_value")); // This should be skipped during encoding

		DataResult<JsonElement> encodedResult = codec.codec().encodeStart(JsonOps.INSTANCE, originalMap);
		assertTrue(encodedResult.result().isPresent(), "Encoding should succeed.");
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("Encoded JSON (Skip Unknown): " + toJson(encodedJson));

		assertTrue(encodedJson.getAsJsonObject().has(A_KEY.key()), "Encoded JSON should contain property_a");
		assertFalse(encodedJson.getAsJsonObject().has("unknown_property"), "Encoded JSON should not contain unknown_property");
	}

	@Test
	void testDecodeSkipUnknownKey() {
		String jsonString = """
            {
              "property_a": { "value": 15 },
              "unknown_property": "some_value",
              "property_b": { "name": "world" }
            }
            """;
		JsonElement jsonInput = JsonParser.parseString(jsonString);

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding should succeed.");
		Map<String, List<?>> decodedMap = decodedResult.result().get();

		assertEquals(2, decodedMap.size(), "Only property_a and property_b should be decoded");
		assertTrue(decodedMap.containsKey(A_KEY.key()), "Decoded map should contain property_a");
		assertTrue(decodedMap.containsKey(B_KEY.key()), "Decoded map should contain property_b");
		assertFalse(decodedMap.containsKey("unknown_property"), "Decoded map should not contain unknown_property");

		assertEquals(List.of(new TestPropertyA(15)), decodedMap.get(A_KEY.key()), "PropertyA value should match");
		assertEquals(List.of(new TestPropertyB("world")), decodedMap.get(B_KEY.key()), "PropertyB value should match");
	}

	@Test
	void testEncodeEmptyMap() {
		Map<String, List<?>> originalMap = Collections.emptyMap();

		DataResult<JsonElement> encodedResult = codec.codec().encodeStart(JsonOps.INSTANCE, originalMap);
		assertTrue(encodedResult.result().isPresent(), "Encoding empty map should succeed.");
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("Encoded JSON (Empty Map): " + toJson(encodedJson));
		assertTrue(encodedJson.getAsJsonObject().entrySet().isEmpty(), "Encoded JSON object should be empty");
	}

	@Test
	void testDecodeEmptyInput() {
		JsonElement jsonInput = JsonParser.parseString("{}");

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding empty input should succeed.");
		Map<String, List<?>> decodedMap = decodedResult.result().get();

		assertTrue(decodedMap.isEmpty(), "Decoded map should be empty");
	}

	@Test
	void testDecodePartialSuccessWithError() {
		// Here, property_c has a malformed value ("not_a_boolean")
		// property_d is an unknown key
		String jsonString = """
            {
              "property_a": { "value": 10 },
              "property_b": { "name": "valid" },
              "property_c": { "active": "not_a_boolean" },
              "property_d": "another_unknown"
            }
            """;
		JsonElement jsonInput = JsonParser.parseString(jsonString);

		DataResult<Map<String, List<?>>> decodedResult = codec.codec().decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);

		// Expect an error because property_c is malformed, but a partial result for A and B
		assertTrue(decodedResult.error().isPresent(), "Decoding should report an error for malformed data.");
		DataResult.PartialResult<?> error = decodedResult.error().get();
		System.out.println("Decode error message: " + error.message());
		assertTrue(error.message().contains("Failed to decode properties:"), "Error message should indicate failure");
		assertTrue(error.message().contains(C_KEY.key()), "Error message should mention property_c");

		// Check for partial result - valid properties should still be present
		assertTrue(decodedResult.resultOrPartial(c -> {}).isPresent(), "Decoding should provide a partial result.");
		Map<String, List<?>> decodedMap = decodedResult.resultOrPartial(c -> {}).get();

		assertEquals(2, decodedMap.size(), "Only property_a and property_b should be successfully decoded");
		assertTrue(decodedMap.containsKey(A_KEY.key()), "Decoded map should contain property_a");
		assertTrue(decodedMap.containsKey(B_KEY.key()), "Decoded map should contain property_b");
		assertFalse(decodedMap.containsKey("property_d"), "Decoded map should NOT contain unknown property_d");

		assertEquals(List.of(new TestPropertyA(10)), decodedMap.get(A_KEY.key()), "PropertyA value should match");
		assertEquals(List.of(new TestPropertyB("valid")), decodedMap.get(B_KEY.key()), "PropertyB value should match");
	}

	@Test
	void testListCodecWrapperEncodeSingle() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		List<TestPropertyA> singleElementList = List.of(new TestPropertyA(42));

		DataResult<JsonElement> encodedResult = wrapper.encodeStart(JsonOps.INSTANCE, singleElementList);
		assertTrue(encodedResult.result().isPresent(), "Encoding single element should succeed.");
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("ListCodecWrapper Encode Single: " + toJson(encodedJson));
		assertTrue(encodedJson.isJsonObject(), "Single element should encode as a JSON object, not an array.");
		assertEquals(42, encodedJson.getAsJsonObject().get("value").getAsInt());
	}

	@Test
	void testListCodecWrapperEncodeMulti() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		List<TestPropertyA> multiElementList = List.of(new TestPropertyA(1), new TestPropertyA(2));

		DataResult<JsonElement> encodedResult = wrapper.encodeStart(JsonOps.INSTANCE, multiElementList);
		assertTrue(encodedResult.result().isPresent(), "Encoding multi elements should succeed.");
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("ListCodecWrapper Encode Multi: " + toJson(encodedJson));
		assertTrue(encodedJson.isJsonArray(), "Multiple elements should encode as a JSON array.");
		assertEquals(2, encodedJson.getAsJsonArray().size());
		assertEquals(1, encodedJson.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsInt());
		assertEquals(2, encodedJson.getAsJsonArray().get(1).getAsJsonObject().get("value").getAsInt());
	}

	@Test
	void testListCodecWrapperEncodeEmpty() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		List<TestPropertyA> emptyList = Collections.emptyList();

		DataResult<JsonElement> encodedResult = wrapper.encodeStart(JsonOps.INSTANCE, emptyList);
		assertTrue(encodedResult.result().isPresent(), "Encoding empty list should succeed.");
		JsonElement encodedJson = encodedResult.result().get();

		System.out.println("ListCodecWrapper Encode Empty: " + toJson(encodedJson));
		assertTrue(encodedJson.isJsonArray(), "Empty list should encode as an empty JSON array.");
		assertEquals(0, encodedJson.getAsJsonArray().size());
	}

	@Test
	void testListCodecWrapperDecodeSingleValue() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		String jsonString = "{ \"value\": 99 }"; // Single JSON object
		JsonElement jsonInput = JsonParser.parseString(jsonString);

		DataResult<List<TestPropertyA>> decodedResult = wrapper.decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding single value should succeed.");
		List<TestPropertyA> decodedList = decodedResult.result().get();

		assertEquals(1, decodedList.size(), "Single JSON object should decode to a single-element list.");
		assertEquals(new TestPropertyA(99), decodedList.get(0), "Decoded property value should match.");
	}

	@Test
	void testListCodecWrapperDecodeMultiValueArray() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		String jsonString = "[ { \"value\": 11 }, { \"value\": 22 } ]"; // JSON array
		JsonElement jsonInput = JsonParser.parseString(jsonString);

		DataResult<List<TestPropertyA>> decodedResult = wrapper.decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding array should succeed.");
		List<TestPropertyA> decodedList = decodedResult.result().get();

		assertEquals(2, decodedList.size(), "JSON array should decode to a multi-element list.");
		assertEquals(new TestPropertyA(11), decodedList.get(0), "First property value should match.");
		assertEquals(new TestPropertyA(22), decodedList.get(1), "Second property value should match.");
	}

	@Test
	void testListCodecWrapperDecodeEmptyArray() {
		Codec<List<TestPropertyA>> wrapper = ListCodecWrapper.of(TestPropertyA.CODEC);
		String jsonString = "[]"; // Empty JSON array
		JsonElement jsonInput = JsonParser.parseString(jsonString);

		DataResult<List<TestPropertyA>> decodedResult = wrapper.decode(JsonOps.INSTANCE, jsonInput).map(Pair::getFirst);
		assertTrue(decodedResult.result().isPresent(), "Decoding empty array should succeed.");
		List<TestPropertyA> decodedList = decodedResult.result().get();

		assertTrue(decodedList.isEmpty(), "Empty JSON array should decode to an empty list.");
	}

	private static final PropertyKey<TestPropertyA> A_KEY = new PropertyKey<>(TestPropertyA.class, "property_a");
	private static final PropertyKey<TestPropertyB> B_KEY = new PropertyKey<>(TestPropertyB.class, "property_b");
	private static final PropertyKey<TestPropertyC> C_KEY = new PropertyKey<>(TestPropertyC.class, "property_c");

	private record TestPropertyA(int value) {
		public static final Codec<TestPropertyA> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("value").forGetter(TestPropertyA::value)
		).apply(instance, TestPropertyA::new));
	}

	private record TestPropertyB(String name) {
		public static final Codec<TestPropertyB> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(TestPropertyB::name)
		).apply(instance, TestPropertyB::new));
	}

	private record TestPropertyC(boolean active) {
		public static final Codec<TestPropertyC> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("active").forGetter(TestPropertyC::active)
		).apply(instance, TestPropertyC::new));
	}
}
