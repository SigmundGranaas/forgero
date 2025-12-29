package com.sigmundgranaas.forgero.core.status;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.StatusModifierData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.StatusModifierDataCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifierDataCodec Tests")
class StatusModifierDataCodecTest {

	private Codec<StatusModifierData> codec;

	@BeforeEach
	void setUp() {
		codec = StatusModifierDataCodec.createSimple();
	}

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(),
				"Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error"));
		return result.result().get();
	}

	private <T> void parseFailure(Codec<T> codec, String json, String expectedError) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.error().isPresent(), "Parsing should fail for: " + json);
		assertTrue(result.error().get().message().contains(expectedError),
				"Error should contain '" + expectedError + "' but was: " + result.error().get().message());
	}

	@Nested
	@DisplayName("Parsing Full JSON")
	class ParsingFullJson {

		private static final String FULL_JSON = """
				{
				  "id": "forgero:sharp",
				  "display_name": "Sharp",
				  "priority": 5,
				  "target": {
				    "types": ["forgero:tool", "forgero:weapon"],
				    "ids": ["forgero:iron_sword"],
				    "incompatibilities": ["forgero:blunt"]
				  },
				  "chance": 0.04,
				  "attributes": [
				    { "id": "forgero:sharp-damage", "type": "forgero:attack_damage", "computation": 3 }
				  ],
				  "properties": {
				    "forgero:tooltip": [{"text": "Sharper edge"}]
				  }
				}
				""";

		@Test
		@DisplayName("should parse all fields")
		void shouldParseAllFields() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertEquals(OpenIdentifier.parse("forgero:sharp"), data.id());
			assertEquals("Sharp", data.displayName());
			assertEquals(5, data.priority());
			assertEquals(0.04f, data.chance(), 0.001f);
		}

		@Test
		@DisplayName("should parse target types")
		void shouldParseTargetTypes() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertNotNull(data.target());
			assertEquals(2, data.target().getTypes().size());
			assertTrue(data.target().getTypes().contains(OpenIdentifier.parse("forgero:tool")));
			assertTrue(data.target().getTypes().contains(OpenIdentifier.parse("forgero:weapon")));
		}

		@Test
		@DisplayName("should parse target ids")
		void shouldParseTargetIds() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertEquals(1, data.target().getIds().size());
			assertTrue(data.target().getIds().contains(OpenIdentifier.parse("forgero:iron_sword")));
		}

		@Test
		@DisplayName("should parse incompatibilities")
		void shouldParseIncompatibilities() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertEquals(1, data.target().getIncompatibilities().size());
			assertTrue(data.target().getIncompatibilities().contains(OpenIdentifier.parse("forgero:blunt")));
		}

		@Test
		@DisplayName("should parse attributes")
		void shouldParseAttributes() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertNotNull(data.attributes());
			assertEquals(1, data.attributes().size());
			assertEquals(OpenIdentifier.parse("forgero:attack_damage"), data.attributes().get(0).type());
		}

		@Test
		@DisplayName("should parse properties")
		void shouldParseProperties() {
			StatusModifierData data = parseSuccess(codec, FULL_JSON);

			assertNotNull(data.properties());
			assertTrue(data.properties().containsKey("forgero:tooltip"));
		}
	}

	@Nested
	@DisplayName("Parsing Minimal JSON")
	class ParsingMinimalJson {

		@Test
		@DisplayName("should parse with only id")
		void shouldParseWithOnlyId() {
			String json = """
					{
					  "id": "forgero:basic"
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertEquals(OpenIdentifier.parse("forgero:basic"), data.id());
			assertEquals("basic", data.displayName()); // Defaults to path name
			assertEquals(0, data.priority());
			assertEquals(0f, data.chance());
			assertNull(data.target());
			assertNull(data.attributes());
			assertNull(data.properties());
		}

		@Test
		@DisplayName("should use id name as default display name")
		void shouldUseIdNameAsDefaultDisplayName() {
			String json = """
					{
					  "id": "forgero:durable_modifier"
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertEquals("durable_modifier", data.displayName());
		}
	}

	@Nested
	@DisplayName("Optional Fields")
	class OptionalFields {

		@Test
		@DisplayName("should handle missing target")
		void shouldHandleMissingTarget() {
			String json = """
					{
					  "id": "forgero:test",
					  "display_name": "Test"
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertNull(data.target());
			// getTarget() should return empty TargetData
			assertTrue(data.getTarget().getTypes().isEmpty());
		}

		@Test
		@DisplayName("should handle partial target")
		void shouldHandlePartialTarget() {
			String json = """
					{
					  "id": "forgero:test",
					  "target": {
					    "types": ["forgero:tool"]
					  }
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertNotNull(data.target());
			assertEquals(1, data.target().getTypes().size());
			assertTrue(data.target().getIds().isEmpty());
			assertTrue(data.target().getIncompatibilities().isEmpty());
		}

		@Test
		@DisplayName("should handle empty target")
		void shouldHandleEmptyTarget() {
			String json = """
					{
					  "id": "forgero:test",
					  "target": {}
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertNotNull(data.target());
			assertTrue(data.target().getTypes().isEmpty());
		}
	}

	@Nested
	@DisplayName("Validation")
	class Validation {

		@Test
		@DisplayName("should fail when id is missing")
		void shouldFailWhenIdIsMissing() {
			String json = """
					{
					  "display_name": "Test"
					}
					""";

			parseFailure(codec, json, "No key id");
		}

		@Test
		@DisplayName("should fail for invalid id format with uppercase")
		void shouldFailForInvalidIdFormat() {
			String json = """
					{
					  "id": "INVALID:UPPERCASE"
					}
					""";

			// This should fail because OpenIdentifier requires lowercase
			// The codec may throw an exception or return an error result
			try {
				DataResult<StatusModifierData> result = codec.parse(
						JsonOps.INSTANCE,
						JsonParser.parseString(json)
				);
				// If we got a result, it should be an error
				assertTrue(result.error().isPresent(), "Should have error for invalid uppercase id");
			} catch (IllegalArgumentException e) {
				// Expected - OpenIdentifier throws for invalid format
				assertTrue(e.getMessage().contains("Invalid") || e.getMessage().contains("lowercase"));
			}
		}
	}

	@Nested
	@DisplayName("Multiple Attributes")
	class MultipleAttributes {

		@Test
		@DisplayName("should parse multiple attributes")
		void shouldParseMultipleAttributes() {
			String json = """
					{
					  "id": "forgero:powerful",
					  "attributes": [
					    { "type": "forgero:attack_damage", "computation": 5 },
					    { "type": "forgero:durability", "computation": 100 },
					    { "type": "forgero:mining_speed", "computation": 2 }
					  ]
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);

			assertEquals(3, data.attributes().size());
		}
	}

	@Nested
	@DisplayName("Priority Handling")
	class PriorityHandling {

		@Test
		@DisplayName("should parse positive priority")
		void shouldParsePositivePriority() {
			String json = """
					{
					  "id": "forgero:high_priority",
					  "priority": 100
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);
			assertEquals(100, data.priority());
		}

		@Test
		@DisplayName("should parse negative priority")
		void shouldParseNegativePriority() {
			String json = """
					{
					  "id": "forgero:low_priority",
					  "priority": -5
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);
			assertEquals(-5, data.priority());
		}
	}

	@Nested
	@DisplayName("Chance Handling")
	class ChanceHandling {

		@Test
		@DisplayName("should parse zero chance")
		void shouldParseZeroChance() {
			String json = """
					{
					  "id": "forgero:never",
					  "chance": 0.0
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);
			assertEquals(0f, data.chance());
		}

		@Test
		@DisplayName("should parse full chance")
		void shouldParseFullChance() {
			String json = """
					{
					  "id": "forgero:always",
					  "chance": 1.0
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);
			assertEquals(1f, data.chance());
		}

		@Test
		@DisplayName("should parse decimal chance")
		void shouldParseDecimalChance() {
			String json = """
					{
					  "id": "forgero:rare",
					  "chance": 0.0123
					}
					""";

			StatusModifierData data = parseSuccess(codec, json);
			assertEquals(0.0123f, data.chance(), 0.0001f);
		}
	}

	@Nested
	@DisplayName("TargetData Codec")
	class TargetDataCodec {

		@Test
		@DisplayName("should parse target with all fields")
		void shouldParseTargetWithAllFields() {
			String json = """
					{
					  "types": ["forgero:tool"],
					  "ids": ["forgero:specific"],
					  "incompatibilities": ["forgero:conflict"]
					}
					""";

			DataResult<StatusModifierData.TargetData> result =
					StatusModifierDataCodec.TARGET_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));

			assertTrue(result.result().isPresent());
			StatusModifierData.TargetData target = result.result().get();

			assertEquals(1, target.getTypes().size());
			assertEquals(1, target.getIds().size());
			assertEquals(1, target.getIncompatibilities().size());
		}
	}
}
