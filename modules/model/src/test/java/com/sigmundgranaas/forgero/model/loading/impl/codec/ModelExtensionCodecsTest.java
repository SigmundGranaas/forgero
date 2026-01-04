package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelExtensionDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelExtensionCodecs - tests JSON deserialization of model extensions.
 */
class ModelExtensionCodecsTest {

	private ModelExtensionDTO parseSuccess(String json) {
		JsonElement element = JsonParser.parseString(json);
		DataResult<ModelExtensionDTO> result = ModelExtensionCodecs.MODEL_EXTENSION_CODEC.parse(JsonOps.INSTANCE, element);
		assertTrue(result.result().isPresent(),
				"Parsing should succeed. Error: " + result.error().map(e -> e.message()).orElse("No error message"));
		return result.result().get();
	}

	private void parseFailure(String json, String expectedError) {
		JsonElement element = JsonParser.parseString(json);
		DataResult<ModelExtensionDTO> result = ModelExtensionCodecs.MODEL_EXTENSION_CODEC.parse(JsonOps.INSTANCE, element);
		assertTrue(result.error().isPresent(), "Parsing should fail for: " + json);
		assertTrue(result.error().get().message().contains(expectedError),
				"Error message mismatch. Expected to contain '" + expectedError + "', but was: " + result.error().get().message());
	}

	@Nested
	class WhenParsingMinimalExtension {

		@Test
		void shouldParseTypeAndTarget() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head"
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertEquals("forgero:model_extension", dto.type());
			assertEquals("forgero:parts/iron-pickaxe_head", dto.target());
			assertEquals(0, dto.priority()); // Default priority
			assertNull(dto.layers());
			assertNull(dto.slots());
			assertNull(dto.mountPoints());
		}
	}

	@Nested
	class WhenParsingExtensionWithPriority {

		@Test
		void shouldParseExplicitPriority() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head",
						"priority": 100
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertEquals(100, dto.priority());
		}

		@Test
		void shouldParseNegativePriority() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head",
						"priority": -50
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertEquals(-50, dto.priority());
		}
	}

	@Nested
	class WhenParsingExtensionWithLayers {

		@Test
		void shouldParseSingleLayer() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/overlays/dye_overlay"
								}
							}
						]
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertNotNull(dto.layers());
			assertEquals(1, dto.layers().size());
			assertEquals(10, dto.layers().get(0).order());
			assertEquals("forgero:item/overlays/dye_overlay", dto.layers().get(0).textures().defaultTexture());
		}

		@Test
		void shouldParseMultipleLayers() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/overlays/dye_overlay"
								}
							},
							{
								"order": 20,
								"textures": {
									"default": "forgero:item/overlays/glow_overlay"
								}
							}
						]
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertNotNull(dto.layers());
			assertEquals(2, dto.layers().size());
			assertEquals(10, dto.layers().get(0).order());
			assertEquals(20, dto.layers().get(1).order());
		}
	}

	@Nested
	class WhenParsingExtensionWithSlots {

		@Test
		void shouldParseSingleSlot() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:equipment/iron-pickaxe",
						"slots": [
							{
								"id": "dye_slot",
								"order": 5,
								"renderer": {
									"type": "forgero:component"
								}
							}
						]
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertNotNull(dto.slots());
			assertEquals(1, dto.slots().size());
			assertEquals("dye_slot", dto.slots().get(0).id());
			assertEquals(5, dto.slots().get(0).order());
		}
	}

	@Nested
	class WhenParsingExtensionWithMountPoints {

		@Test
		void shouldParseSingleMountPoint() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/handle",
						"mount_points": [
							{
								"name": "charm_mount",
								"position": [8, 2]
							}
						]
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertNotNull(dto.mountPoints());
			assertEquals(1, dto.mountPoints().size());
			assertEquals("charm_mount", dto.mountPoints().get(0).name());
			assertEquals(2, dto.mountPoints().get(0).position().size());
			assertEquals(8, dto.mountPoints().get(0).position().get(0));
			assertEquals(2, dto.mountPoints().get(0).position().get(1));
		}
	}

	@Nested
	class WhenParsingFullExtension {

		@Test
		void shouldParseAllFields() {
			String json = """
					{
						"type": "forgero:model_extension",
						"target": "forgero:parts/iron-pickaxe_head",
						"priority": 100,
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/overlays/dye_overlay"
								}
							}
						],
						"slots": [
							{
								"id": "dye_slot",
								"order": 5,
								"renderer": {
									"type": "forgero:component"
								}
							}
						],
						"mount_points": [
							{
								"name": "charm_mount",
								"position": [8, 2]
							}
						]
					}
					""";

			ModelExtensionDTO dto = parseSuccess(json);

			assertEquals("forgero:model_extension", dto.type());
			assertEquals("forgero:parts/iron-pickaxe_head", dto.target());
			assertEquals(100, dto.priority());
			assertNotNull(dto.layers());
			assertEquals(1, dto.layers().size());
			assertNotNull(dto.slots());
			assertEquals(1, dto.slots().size());
			assertNotNull(dto.mountPoints());
			assertEquals(1, dto.mountPoints().size());
		}
	}

	@Nested
	class WhenParsingInvalidExtension {

		@Test
		void shouldFailWhenMissingType() {
			String json = """
					{
						"target": "forgero:parts/iron-pickaxe_head"
					}
					""";

			parseFailure(json, "type");
		}

		@Test
		void shouldFailWhenMissingTarget() {
			String json = """
					{
						"type": "forgero:model_extension"
					}
					""";

			parseFailure(json, "target");
		}
	}
}
