package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.ModelVariant;
import com.sigmundgranaas.forgero.model.api.item.CompositeModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.TexturesDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.VariantDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for handle model variants with root_tag predicates.
 * 
 * This test suite verifies that handle parts can use different textures
 * based on what tool type they are attached to (pickaxe, axe, sword, etc.)
 * using the root_tag predicate system.
 * 
 * The key use case is: A generic "oak-handle" should display as
 * "oak-pickaxe_handle" when part of a pickaxe, "oak-axe_handle" when
 * part of an axe, etc.
 */
class HandleModelVariantTest {

	@Nested
	@DisplayName("Codec Parsing Tests")
	class CodecParsingTests {

		@Test
		@DisplayName("TexturesDTO parses variants with root_tag predicates")
		void testTexturesDTOWithRootTagVariants() {
			String json = """
					{
						"default": "forgero:item/oak-handle",
						"variants": [
							{
								"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
								"texture": "forgero:item/oak-pickaxe_handle"
							},
							{
								"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
								"texture": "forgero:item/oak-axe_handle"
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TexturesDTO> result = TexturesDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Should parse textures with root_tag variants");
			TexturesDTO textures = result.result().get();

			assertEquals("forgero:item/oak-handle", textures.defaultTexture());
			assertNotNull(textures.variants(), "Variants should not be null");
			assertEquals(2, textures.variants().size(), "Should have 2 variants");

			// Verify first variant
			VariantDTO pickaxeVariant = textures.variants().get(0);
			assertEquals("forgero:item/oak-pickaxe_handle", pickaxeVariant.texture());
			assertEquals(1, pickaxeVariant.predicate().size());
			assertEquals("forgero:root_tag", pickaxeVariant.predicate().get(0).type());
			assertEquals("forgero:tools/pickaxe", pickaxeVariant.predicate().get(0).tag());
		}

		@Test
		@DisplayName("LayerDTO parses textures with variants")
		void testLayerDTOWithVariants() {
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
			DataResult<LayerDTO> result = ModelCodecs.LAYER_DTO_CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Should parse layer with texture variants");
			LayerDTO layer = result.result().get();

			assertEquals(10, layer.order());
			assertNotNull(layer.textures());
			assertEquals("forgero:item/oak-handle", layer.textures().defaultTexture());
			assertNotNull(layer.textures().variants());
			assertEquals(1, layer.textures().variants().size());
		}

		@Test
		@DisplayName("ModelDTO parses composite model with layer variants")
		void testModelDTOWithLayerVariants() {
			String json = """
					{
						"type": "forgero:composite_model",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/oak-handle",
									"variants": [
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
											"texture": "forgero:item/oak-pickaxe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
											"texture": "forgero:item/oak-axe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/hoe" }],
											"texture": "forgero:item/oak-hoe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/shovel" }],
											"texture": "forgero:item/oak-medium_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:weapons/sword" }],
											"texture": "forgero:item/oak-short_handle"
										}
									]
								}
							}
						],
						"slots": [
							{
								"id": "grip_slot",
								"order": 25,
								"renderer": { "type": "forgero:component", "context": "grip" }
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<ModelDTO> result = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Should parse composite model with layer variants");
			ModelDTO model = result.result().get();

			assertEquals("forgero:composite_model", model.type());
			assertTrue(model.getLayers().isPresent());
			assertEquals(1, model.getLayers().get().size());

			LayerDTO layer = model.getLayers().get().get(0);
			assertNotNull(layer.textures().variants());
			assertEquals(5, layer.textures().variants().size(), "Should have 5 tool-type variants");
		}

		@Test
		@DisplayName("Predicate with multiple conditions (AND logic)")
		void testMultiplePredicatesAND() {
			String json = """
					{
						"default": "forgero:item/oak-handle",
						"variants": [
							{
								"predicate": [
									{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" },
									{ "type": "forgero:child_tag", "tag": "forgero:reinforced" }
								],
								"texture": "forgero:item/oak-reinforced_pickaxe_handle"
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<TexturesDTO> result = TexturesDTO.CODEC.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent());
			TexturesDTO textures = result.result().get();

			VariantDTO variant = textures.variants().get(0);
			assertEquals(2, variant.predicate().size(), "Should have 2 predicates (AND logic)");
			assertEquals("forgero:root_tag", variant.predicate().get(0).type());
			assertEquals("forgero:child_tag", variant.predicate().get(1).type());
		}
	}

	@Nested
	@DisplayName("Model Translation Tests")
	class ModelTranslationTests {

		@Test
		@DisplayName("ModelTranslator converts layer variants to ModelVariant objects")
		void testModelTranslatorCreatesVariants() {
			String json = """
					{
						"type": "forgero:composite_model",
						"layers": [
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
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			ModelDTO dto = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element).result().orElseThrow();

			// Use ModelTranslator to convert to domain model
			ModelTranslator translator = new ModelTranslator();
			Model model = translator.toDomain(OpenIdentifier.of("forgero", "test/oak-handle"), dto);

			assertInstanceOf(CompositeModel.class, model);
			CompositeModel compositeModel = (CompositeModel) model;

			assertEquals(1, compositeModel.layers().size());
			ModelLayer layer = compositeModel.layers().get(0);

			assertEquals("forgero:item/oak-handle", layer.texture());
			assertFalse(layer.variants().isEmpty(), "Layer should have variants");
			assertEquals(1, layer.variants().size());

			ModelVariant variant = layer.variants().get(0);
			assertTrue(variant.texture().isPresent());
			assertEquals("forgero:item/oak-pickaxe_handle", variant.texture().get());
			assertEquals(1, variant.predicate().size(), "Variant should have 1 predicate");
		}

		@Test
		@DisplayName("Translated model has all 5 tool-type variants")
		void testAllToolTypeVariantsPreserved() {
			String json = """
					{
						"type": "forgero:composite_model",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/oak-handle",
									"variants": [
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
											"texture": "forgero:item/oak-pickaxe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
											"texture": "forgero:item/oak-axe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/hoe" }],
											"texture": "forgero:item/oak-hoe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/shovel" }],
											"texture": "forgero:item/oak-medium_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:weapons/sword" }],
											"texture": "forgero:item/oak-short_handle"
										}
									]
								}
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			ModelDTO dto = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element).result().orElseThrow();

			ModelTranslator translator = new ModelTranslator();
			CompositeModel model = (CompositeModel) translator.toDomain(OpenIdentifier.of("forgero", "test/oak-handle"), dto);

			ModelLayer layer = model.layers().get(0);
			assertEquals(5, layer.variants().size(), "Should preserve all 5 tool-type variants");

			// Verify each variant has the correct texture
			List<String> expectedTextures = List.of(
					"forgero:item/oak-pickaxe_handle",
					"forgero:item/oak-axe_handle",
					"forgero:item/oak-hoe_handle",
					"forgero:item/oak-medium_handle",
					"forgero:item/oak-short_handle"
			);

			for (int i = 0; i < 5; i++) {
				ModelVariant variant = layer.variants().get(i);
				assertTrue(variant.texture().isPresent());
				assertEquals(expectedTextures.get(i), variant.texture().get());
			}
		}
	}

	@Nested
	@DisplayName("Variant Selection Tests")
	class VariantSelectionTests {

		@Test
		@DisplayName("Layer selects pickaxe_handle variant when root has pickaxe tag")
		void testPickaxeVariantSelection() {
			// Create a layer with variants
			ModelLayer layer = createHandleLayerWithVariants();

			// Create a mock context where root has the pickaxe tag
			ModelResolutionContext pickaxeContext = createMockContext("forgero:tools/pickaxe");

			// Get the active variant
			var activeVariant = layer.getActiveVariant(pickaxeContext);

			assertTrue(activeVariant.isPresent(), "Should find matching variant for pickaxe");
			assertEquals("forgero:item/oak-pickaxe_handle", activeVariant.get().texture().orElse(""));
		}

		@Test
		@DisplayName("Layer selects axe_handle variant when root has axe tag")
		void testAxeVariantSelection() {
			ModelLayer layer = createHandleLayerWithVariants();
			ModelResolutionContext axeContext = createMockContext("forgero:tools/axe");

			var activeVariant = layer.getActiveVariant(axeContext);

			assertTrue(activeVariant.isPresent(), "Should find matching variant for axe");
			assertEquals("forgero:item/oak-axe_handle", activeVariant.get().texture().orElse(""));
		}

		@Test
		@DisplayName("Layer uses default texture when no variant matches")
		void testDefaultTextureWhenNoMatch() {
			ModelLayer layer = createHandleLayerWithVariants();

			// Create context with a tag that doesn't match any variant
			ModelResolutionContext unknownContext = createMockContext("forgero:tools/unknown_tool");

			var activeVariant = layer.getActiveVariant(unknownContext);

			assertFalse(activeVariant.isPresent(), "Should not find matching variant for unknown tool");
			// The layer's default texture should be used
			assertEquals("forgero:item/oak-handle", layer.texture());
		}

		private ModelLayer createHandleLayerWithVariants() {
			// Parse a handle model with variants
			String json = """
					{
						"type": "forgero:composite_model",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/oak-handle",
									"variants": [
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
											"texture": "forgero:item/oak-pickaxe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
											"texture": "forgero:item/oak-axe_handle"
										}
									]
								}
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			ModelDTO dto = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element).result().orElseThrow();

			ModelTranslator translator = new ModelTranslator();
			CompositeModel model = (CompositeModel) translator.toDomain(OpenIdentifier.of("forgero", "test"), dto);

			return model.layers().get(0);
		}

		private ModelResolutionContext createMockContext(String rootTag) {
			// Use StaticComponent from the actual implementation
			Component mockComponent = new StaticComponent(
					OpenIdentifier.of("forgero", "test-component"),
					Set.of(OpenIdentifier.parse(rootTag)),
					new HashMap<>()
			);
			return new ModelResolutionContext(mockComponent, mockComponent);
		}
	}

	@Nested
	@DisplayName("Real Handle Model JSON Structure Tests")
	class RealHandleModelTests {

		/**
		 * Tests the exact JSON structure used in the actual handle.json model template.
		 * This ensures the format we're using in content/forgero-base is valid.
		 */
		@Test
		@DisplayName("Actual handle.json format with all tool variants parses correctly")
		void testActualHandleJsonFormat() {
			// This matches the structure in content/forgero-base/src/main/resources/assets/forgero/forgero_models/item/handle.json
			String json = """
					{
						"type": "forgero:composite_model",
						"target": "forgero:oak-handle",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/oak-handle",
									"variants": [
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
											"texture": "forgero:item/oak-pickaxe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
											"texture": "forgero:item/oak-axe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/hoe" }],
											"texture": "forgero:item/oak-hoe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/shovel" }],
											"texture": "forgero:item/oak-medium_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:weapons/sword" }],
											"texture": "forgero:item/oak-short_handle"
										}
									]
								}
							}
						],
						"slots": [
							{
								"id": "grip_slot",
								"order": 25,
								"renderer": {
									"type": "forgero:component",
									"context": "grip"
								}
							},
							{
								"id": "pommel_slot",
								"order": 5,
								"renderer": {
									"type": "forgero:component",
									"context": "pommel"
								}
							},
							{
								"id": "handle-cosmetic",
								"order": 15,
								"renderer": {
									"type": "forgero:component",
									"context": "handle_dye"
								}
							}
						]
					}
					""";

			JsonElement element = JsonParser.parseString(json);
			DataResult<ModelDTO> result = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element);

			assertTrue(result.result().isPresent(), "Actual handle.json format should parse successfully");
			ModelDTO model = result.result().get();

			// Verify structure
			assertEquals("forgero:composite_model", model.type());
			assertTrue(model.getTarget().isPresent());
			assertEquals("forgero:oak-handle", model.getTarget().get());

			// Verify layers
			assertTrue(model.getLayers().isPresent());
			assertEquals(1, model.getLayers().get().size());
			LayerDTO layer = model.getLayers().get().get(0);
			assertEquals(10, layer.order());
			assertEquals("forgero:item/oak-handle", layer.textures().defaultTexture());
			assertEquals(5, layer.textures().variants().size(), "Should have 5 tool-type variants");

			// Verify slots
			assertTrue(model.getSlots().isPresent());
			assertEquals(3, model.getSlots().get().size(), "Should have 3 slots (grip, pommel, cosmetic)");
		}

		@Test
		@DisplayName("Handle model can be translated and used for variant selection")
		void testHandleModelEndToEnd() {
			String json = """
					{
						"type": "forgero:composite_model",
						"layers": [
							{
								"order": 10,
								"textures": {
									"default": "forgero:item/oak-handle",
									"variants": [
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/pickaxe" }],
											"texture": "forgero:item/oak-pickaxe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/axe" }],
											"texture": "forgero:item/oak-axe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/hoe" }],
											"texture": "forgero:item/oak-hoe_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:tools/shovel" }],
											"texture": "forgero:item/oak-medium_handle"
										},
										{
											"predicate": [{ "type": "forgero:root_tag", "tag": "forgero:weapons/sword" }],
											"texture": "forgero:item/oak-short_handle"
										}
									]
								}
							}
						]
					}
					""";

			// Parse
			JsonElement element = JsonParser.parseString(json);
			ModelDTO dto = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, element).result().orElseThrow();

			// Translate
			ModelTranslator translator = new ModelTranslator();
			CompositeModel model = (CompositeModel) translator.toDomain(OpenIdentifier.of("forgero", "parts/oak-handle"), dto);

			// Verify variants are properly translated
			ModelLayer layer = model.layers().get(0);
			assertEquals(5, layer.variants().size());

			// Test each tool type selection
			String[] toolTags = {
					"forgero:tools/pickaxe",
					"forgero:tools/axe",
					"forgero:tools/hoe",
					"forgero:tools/shovel",
					"forgero:weapons/sword"
			};
			String[] expectedTextures = {
					"forgero:item/oak-pickaxe_handle",
					"forgero:item/oak-axe_handle",
					"forgero:item/oak-hoe_handle",
					"forgero:item/oak-medium_handle",
					"forgero:item/oak-short_handle"
			};

			for (int i = 0; i < toolTags.length; i++) {
				Component mockRoot = new StaticComponent(
						OpenIdentifier.of("forgero", "test"),
						Set.of(OpenIdentifier.parse(toolTags[i])),
						new HashMap<>()
				);
				ModelResolutionContext context = new ModelResolutionContext(mockRoot, mockRoot);

				var activeVariant = layer.getActiveVariant(context);
				assertTrue(activeVariant.isPresent(), "Should find variant for " + toolTags[i]);
				assertEquals(expectedTextures[i], activeVariant.get().texture().orElse(""),
						"Wrong texture for " + toolTags[i]);
			}
		}
	}
}
