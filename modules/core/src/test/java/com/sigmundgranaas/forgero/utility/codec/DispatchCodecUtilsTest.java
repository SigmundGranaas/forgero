package com.sigmundgranaas.forgero.utility.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DispatchCodecUtilsTest extends ForgeroTest {

	// Test interface representing a polymorphic handler
	interface TestHandler {
		String type();
		String getValue();
	}

	// First implementation
	record SimpleHandler(String value) implements TestHandler {
		public static final Codec<SimpleHandler> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("value").forGetter(SimpleHandler::value)
				).apply(instance, SimpleHandler::new)
		);

		@Override
		public String type() {
			return "simple";
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	// Second implementation
	record ComplexHandler(String value, int count) implements TestHandler {
		public static final Codec<ComplexHandler> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("value").forGetter(ComplexHandler::value),
						Codec.INT.fieldOf("count").forGetter(ComplexHandler::count)
				).apply(instance, ComplexHandler::new)
		);

		@Override
		public String type() {
			return "complex";
		}

		@Override
		public String getValue() {
			return value + " (count: " + count + ")";
		}
	}

	// Codec registry for test handlers
	private static final Map<String, Codec<? extends TestHandler>> REGISTRY = new HashMap<>();

	static {
		REGISTRY.put("simple", SimpleHandler.CODEC);
		REGISTRY.put("complex", ComplexHandler.CODEC);
	}

	private static Codec<? extends TestHandler> getCodec(String type) {
		Codec<? extends TestHandler> codec = REGISTRY.get(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
		return codec;
	}

	private final Codec<TestHandler> dispatchCodec = DispatchCodecUtils.create(
			DispatchCodecUtilsTest::getCodec,
			TestHandler::type
	);

	@Test
	void encodesSimpleImplementation() {
		// Create a simple handler
		TestHandler handler = new SimpleHandler("test_value");

		// Encode it
		DataResult<JsonElement> result = dispatchCodec.encodeStart(JsonOps.INSTANCE, handler);

		assertTrue(result.result().isPresent(), "Encoding should succeed");

		JsonElement json = result.result().get();
		assertTrue(json.isJsonObject(), "Result should be a JSON object");

		// Verify the JSON contains both the type field and the value field
		assertEquals("simple", json.getAsJsonObject().get("type").getAsString());
		assertEquals("test_value", json.getAsJsonObject().get("value").getAsString());
	}

	@Test
	void encodesComplexImplementation() {
		// Create a complex handler
		TestHandler handler = new ComplexHandler("complex_value", 42);

		// Encode it
		DataResult<JsonElement> result = dispatchCodec.encodeStart(JsonOps.INSTANCE, handler);

		assertTrue(result.result().isPresent(), "Encoding should succeed");

		JsonElement json = result.result().get();
		assertTrue(json.isJsonObject(), "Result should be a JSON object");

		// Verify all fields are present
		assertEquals("complex", json.getAsJsonObject().get("type").getAsString());
		assertEquals("complex_value", json.getAsJsonObject().get("value").getAsString());
		assertEquals(42, json.getAsJsonObject().get("count").getAsInt());
	}

	@Test
	void decodesSimpleImplementation() {
		// Create JSON for a simple handler
		String json = "{\"type\": \"simple\", \"value\": \"decoded_value\"}";
		JsonElement jsonElement = JsonParser.parseString(json);

		// Decode it
		DataResult<TestHandler> result = dispatchCodec.parse(JsonOps.INSTANCE, jsonElement);

		assertTrue(result.result().isPresent(), "Decoding should succeed");

		TestHandler handler = result.result().get();
		assertInstanceOf(SimpleHandler.class, handler, "Should decode to SimpleHandler");
		assertEquals("simple", handler.type());
		assertEquals("decoded_value", handler.getValue());
	}

	@Test
	void decodesComplexImplementation() {
		// Create JSON for a complex handler
		String json = "{\"type\": \"complex\", \"value\": \"decoded_complex\", \"count\": 99}";
		JsonElement jsonElement = JsonParser.parseString(json);

		// Decode it
		DataResult<TestHandler> result = dispatchCodec.parse(JsonOps.INSTANCE, jsonElement);

		assertTrue(result.result().isPresent(), "Decoding should succeed");

		TestHandler handler = result.result().get();
		assertInstanceOf(ComplexHandler.class, handler, "Should decode to ComplexHandler");
		assertEquals("complex", handler.type());
		assertEquals("decoded_complex (count: 99)", handler.getValue());
	}

	@Test
	void roundTripPreservesData() {
		// Create a handler, encode it, then decode it
		TestHandler original = new ComplexHandler("roundtrip_test", 123);

		// Encode
		DataResult<JsonElement> encoded = dispatchCodec.encodeStart(JsonOps.INSTANCE, original);
		assertTrue(encoded.result().isPresent());

		// Decode
		DataResult<TestHandler> decoded = dispatchCodec.parse(JsonOps.INSTANCE, encoded.result().get());
		assertTrue(decoded.result().isPresent());

		// Verify it matches
		TestHandler roundtripped = decoded.result().get();
		assertEquals(original.type(), roundtripped.type());
		assertEquals(original.getValue(), roundtripped.getValue());
	}

	@Test
	void failsOnMissingTypeField() {
		// JSON without a "type" field
		String json = "{\"value\": \"no_type\"}";
		JsonElement jsonElement = JsonParser.parseString(json);

		// Attempt to decode
		DataResult<TestHandler> result = dispatchCodec.parse(JsonOps.INSTANCE, jsonElement);

		assertTrue(result.error().isPresent(), "Should fail when type field is missing");
	}

	@Test
	void failsOnUnknownType() {
		// JSON with an unknown type
		String json = "{\"type\": \"unknown\", \"value\": \"test\"}";
		JsonElement jsonElement = JsonParser.parseString(json);

		// Attempt to decode - should throw IllegalArgumentException from getCodec
		assertThrows(IllegalArgumentException.class, () -> {
			dispatchCodec.parse(JsonOps.INSTANCE, jsonElement);
		}, "Should throw exception for unknown type");
	}

	@Test
	void failsOnInvalidJsonStructure() {
		// JSON that doesn't match the expected codec structure
		String json = "{\"type\": \"simple\", \"wrong_field\": \"value\"}";
		JsonElement jsonElement = JsonParser.parseString(json);

		// Attempt to decode
		DataResult<TestHandler> result = dispatchCodec.parse(JsonOps.INSTANCE, jsonElement);

		assertTrue(result.error().isPresent(), "Should fail when required fields are missing");
	}
}
