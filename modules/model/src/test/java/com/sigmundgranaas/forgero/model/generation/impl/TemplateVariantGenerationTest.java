package com.sigmundgranaas.forgero.model.generation.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateModelDTO.TemplateLayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateModelDTO.TemplateTexturesDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateModelDTO.TemplateVariantDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Template Variant Generation Tests")
class TemplateVariantGenerationTest {

	@Nested
	@DisplayName("TemplateLayerDTO Codec Tests")
	class TemplateLayerCodecTests {

		@Test
		@DisplayName("TemplateLayerDTO parses layer with textures and variants")
		void testTemplateLayerWithTexturesAndVariants() {
			String json = """
					{
						"order": 10,
						"template": "forgero:texture_template/parts/handle",
						"palette": "forgero:palette/{target.name}",
						"output": "forgero:item/{target.name}-handle",
						"textures": {
							"default": "forgero:item/{target.name}-handle",
							"variants": [
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
									"texture": "forgero:item/{target.name}-pickaxe_handle"
								},
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
									"texture": "forgero:item/{target.name}-axe_handle"
								}
							]
						}
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateLayerDTO> result = TemplateLayerDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Should parse template layer with textures");
			TemplateLayerDTO layer = result.result().get();

			assertEquals(10, layer.order());
			assertEquals("forgero:texture_template/parts/handle", layer.template());
			assertEquals("forgero:palette/{target.name}", layer.palette());
			assertEquals("forgero:item/{target.name}-handle", layer.output());

			assertNotNull(layer.textures());
			assertEquals("forgero:item/{target.name}-handle", layer.textures().defaultTexture());
			assertNotNull(layer.textures().variants());
			assertEquals(2, layer.textures().variants().size());

			TemplateVariantDTO pickaxeVariant = layer.textures().variants().get(0);
			assertEquals("forgero:item/{target.name}-pickaxe_handle", pickaxeVariant.texture());
			assertEquals(1, pickaxeVariant.predicate().size());
			assertEquals("forgero:root_tag", pickaxeVariant.predicate().get(0).type());
			assertEquals("forgero:tools/pickaxe", pickaxeVariant.predicate().get(0).tag());
		}

		@Test
		@DisplayName("TemplateLayerDTO parses layer with only generation fields (backwards compatible)")
		void testTemplateLayerWithOnlyGenerationFields() {
			String json = """
					{
						"order": 10,
						"template": "forgero:texture_template/parts/handle",
						"palette": "forgero:palette/{target.name}",
						"output": "forgero:item/{target.name}-handle"
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateLayerDTO> result = TemplateLayerDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent());
			TemplateLayerDTO layer = result.result().get();

			assertEquals(10, layer.order());
			assertEquals("forgero:texture_template/parts/handle", layer.template());
			assertNull(layer.textures());
		}

		@Test
		@DisplayName("TemplateLayerDTO parses layer with only textures (no generation)")
		void testTemplateLayerWithOnlyTextures() {
			String json = """
					{
						"order": 10,
						"textures": {
							"default": "forgero:item/oak-handle",
							"variants": [
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
									"texture": "forgero:item/oak-pickaxe_handle"
								}
							]
						}
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateLayerDTO> result = TemplateLayerDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent());
			TemplateLayerDTO layer = result.result().get();

			assertEquals(10, layer.order());
			assertNull(layer.template());
			assertNotNull(layer.textures());
			assertEquals("forgero:item/oak-handle", layer.textures().defaultTexture());
			assertEquals(1, layer.textures().variants().size());
		}

		@Test
		@DisplayName("TemplateTexturesDTO parses default and variants")
		void testTemplateTexturesDTOCodec() {
			String json = """
					{
						"default": "forgero:item/{target.name}-handle",
						"variants": [
							{
								"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
								"texture": "forgero:item/{target.name}-pickaxe_handle"
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateTexturesDTO> result = TemplateTexturesDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent());
			TemplateTexturesDTO textures = result.result().get();

			assertEquals("forgero:item/{target.name}-handle", textures.defaultTexture());
			assertNotNull(textures.variants());
			assertEquals(1, textures.variants().size());
		}

		@Test
		@DisplayName("TemplateVariantDTO parses predicate and texture")
		void testTemplateVariantDTOCodec() {
			String json = """
					{
						"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
						"texture": "forgero:item/{target.name}-pickaxe_handle"
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateVariantDTO> result = TemplateVariantDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent());
			TemplateVariantDTO variant = result.result().get();

			assertEquals("forgero:item/{target.name}-pickaxe_handle", variant.texture());
			assertEquals(1, variant.predicate().size());
			assertEquals("forgero:root_tag", variant.predicate().get(0).type());
			assertEquals("forgero:tools/pickaxe", variant.predicate().get(0).tag());
		}

		@Test
		@DisplayName("Full handle.json template format parses correctly")
		void testFullHandleTemplateFormat() {
			String json = """
					{
						"order": 10,
						"template": "forgero:texture_template/parts/handle",
						"palette": "forgero:palette/{target.name}",
						"output": "forgero:item/{target.name}-handle",
						"textures": {
							"default": "forgero:item/{target.name}-handle",
							"variants": [
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
									"texture": "forgero:item/{target.name}-pickaxe_handle"
								},
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
									"texture": "forgero:item/{target.name}-axe_handle"
								},
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/hoe" }],
									"texture": "forgero:item/{target.name}-hoe_handle"
								},
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/shovel" }],
									"texture": "forgero:item/{target.name}-medium_handle"
								},
								{
									"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:weapons/sword" }],
									"texture": "forgero:item/{target.name}-short_handle"
								}
							]
						}
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TemplateLayerDTO> result = TemplateLayerDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Full handle template format should parse");
			TemplateLayerDTO layer = result.result().get();

			assertEquals(10, layer.order());
			assertEquals("forgero:texture_template/parts/handle", layer.template());
			assertEquals("forgero:palette/{target.name}", layer.palette());
			assertEquals("forgero:item/{target.name}-handle", layer.output());

			assertNotNull(layer.textures());
			assertEquals("forgero:item/{target.name}-handle", layer.textures().defaultTexture());
			assertEquals(5, layer.textures().variants().size(), "Should have 5 tool-type variants");

			String[] expectedTags = {
					"forgero:tools/pickaxe",
					"forgero:tools/axe",
					"forgero:tools/hoe",
					"forgero:tools/shovel",
					"forgero:weapons/sword"
			};

			String[] expectedTextures = {
					"forgero:item/{target.name}-pickaxe_handle",
					"forgero:item/{target.name}-axe_handle",
					"forgero:item/{target.name}-hoe_handle",
					"forgero:item/{target.name}-medium_handle",
					"forgero:item/{target.name}-short_handle"
			};

			for (int i = 0; i < 5; i++) {
				TemplateVariantDTO variant = layer.textures().variants().get(i);
				assertEquals(expectedTags[i], variant.predicate().get(0).tag());
				assertEquals(expectedTextures[i], variant.texture());
			}
		}
	}
}
