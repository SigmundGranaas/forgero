package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData;
import com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.GenerationConfigCodecs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenerationFilterCodecTest {

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).map(pair -> pair.getFirst());
		assertTrue(result.result().isPresent(), "Expected successful parse but got error: " + result.error().map(DataResult.PartialResult::message).orElse(""));
		return result.result().get();
	}

	private <T> void parseFailure(Codec<T> codec, String json, String expectedErrorSubstring) {
		DataResult<T> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).map(pair -> pair.getFirst());
		assertTrue(result.error().isPresent(), "Expected parse error but succeeded");
		String errorMessage = result.error().get().message();
		assertTrue(errorMessage.contains(expectedErrorSubstring),
				"Error message '" + errorMessage + "' does not contain '" + expectedErrorSubstring + "'");
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}

	// SlotGenerationFilter Tests

	@Test
	void testParseEmptyFilter() {
		String json = "{}";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertTrue(filter.isEmpty());
		assertNull(filter.requireAllTags());
		assertNull(filter.requireAnyTags());
		assertNull(filter.excludeAnyTags());
		assertNull(filter.excludeAllTags());
		assertNull(filter.explicitList());
	}

	@Test
	void testParseFilterWithRequireAllTags() {
		String json = """
				{
				  "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAllTags());
		assertEquals(2, filter.requireAllTags().size());
		assertTrue(filter.requireAllTags().contains(id("forgero:pickaxe_head_shape")));
		assertTrue(filter.requireAllTags().contains(id("forgero:base_shape")));
	}

	@Test
	void testParseFilterWithRequireAnyTags() {
		String json = """
				{
				  "require_any_tags": ["forgero:metal", "forgero:wood"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAnyTags());
		assertEquals(2, filter.requireAnyTags().size());
	}

	@Test
	void testParseFilterWithExcludeAnyTags() {
		String json = """
				{
				  "exclude_any_tags": ["forgero:schematic", "forgero:cast"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.excludeAnyTags());
		assertEquals(2, filter.excludeAnyTags().size());
		assertTrue(filter.excludeAnyTags().contains(id("forgero:schematic")));
		assertTrue(filter.excludeAnyTags().contains(id("forgero:cast")));
	}

	@Test
	void testParseFilterWithExcludeAllTags() {
		String json = """
				{
				  "exclude_all_tags": ["forgero:experimental", "forgero:disabled"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.excludeAllTags());
		assertEquals(2, filter.excludeAllTags().size());
	}

	@Test
	void testParseFilterWithExplicitList() {
		String json = """
				{
				  "explicit_list": ["forgero:iron", "forgero:gold", "forgero:diamond"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.explicitList());
		assertEquals(3, filter.explicitList().size());
		assertTrue(filter.explicitList().contains(id("forgero:iron")));
		assertTrue(filter.explicitList().contains(id("forgero:gold")));
		assertTrue(filter.explicitList().contains(id("forgero:diamond")));
	}

	@Test
	void testParseFilterWithMultipleConstraints() {
		String json = """
				{
				  "require_all_tags": ["forgero:tool_part"],
				  "exclude_any_tags": ["forgero:deprecated"],
				  "require_any_tags": ["forgero:common", "forgero:rare"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAllTags());
		assertEquals(1, filter.requireAllTags().size());
		assertNotNull(filter.excludeAnyTags());
		assertEquals(1, filter.excludeAnyTags().size());
		assertNotNull(filter.requireAnyTags());
		assertEquals(2, filter.requireAnyTags().size());
	}

	@Test
	void testParseFilterWithAllFields() {
		String json = """
				{
				  "require_all_tags": ["forgero:pickaxe_head_shape"],
				  "require_any_tags": ["forgero:common", "forgero:rare"],
				  "exclude_any_tags": ["forgero:deprecated"],
				  "exclude_all_tags": ["forgero:experimental", "forgero:broken"],
				  "explicit_list": ["forgero:iron-pickaxe_head"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAllTags());
		assertNotNull(filter.requireAnyTags());
		assertNotNull(filter.excludeAnyTags());
		assertNotNull(filter.excludeAllTags());
		assertNotNull(filter.explicitList());
	}

	// GenerationConfigData Tests

	@Test
	void testParseEmptyGenerationConfig() {
		String json = "{}";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertTrue(config.isEmpty());
		assertNull(config.slots());
	}

	@Test
	void testParseGenerationConfigWithSingleSlot() {
		String json = """
				{
				  "slots": {
					"shape": {
					  "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"]
					}
				  }
				}
				""";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertFalse(config.isEmpty());
		assertNotNull(config.slots());
		assertEquals(1, config.slots().size());
		assertTrue(config.slots().containsKey("shape"));

		SlotGenerationFilter shapeFilter = config.getFilterForSlot("shape");
		assertNotNull(shapeFilter);
		assertNotNull(shapeFilter.requireAllTags());
		assertEquals(2, shapeFilter.requireAllTags().size());
	}

	@Test
	void testParseGenerationConfigWithMultipleSlots() {
		String json = """
				{
				  "slots": {
					"shape": {
					  "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"]
					},
					"material": {
					  "require_any_tags": ["forgero:metal", "forgero:gem"]
					},
					"binding": {
					  "exclude_any_tags": ["forgero:deprecated"]
					}
				  }
				}
				""";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertFalse(config.isEmpty());
		assertNotNull(config.slots());
		assertEquals(3, config.slots().size());

		SlotGenerationFilter shapeFilter = config.getFilterForSlot("shape");
		assertNotNull(shapeFilter);
		assertNotNull(shapeFilter.requireAllTags());

		SlotGenerationFilter materialFilter = config.getFilterForSlot("material");
		assertNotNull(materialFilter);
		assertNotNull(materialFilter.requireAnyTags());

		SlotGenerationFilter bindingFilter = config.getFilterForSlot("binding");
		assertNotNull(bindingFilter);
		assertNotNull(bindingFilter.excludeAnyTags());
	}

	@Test
	void testGetFilterForNonExistentSlot() {
		String json = """
				{
				  "slots": {
					"shape": {
					  "require_all_tags": ["forgero:pickaxe_head_shape"]
					}
				  }
				}
				""";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertNull(config.getFilterForSlot("nonexistent"));
	}

	@Test
	void testGetFilterForSlotWhenEmpty() {
		String json = "{}";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertNull(config.getFilterForSlot("shape"));
	}

	@Test
	void testParseGenerationConfigWithComplexFilters() {
		String json = """
				{
				  "slots": {
					"primary": {
					  "require_all_tags": ["forgero:primary_part", "forgero:tool_part"],
					  "exclude_any_tags": ["forgero:schematic", "forgero:cast"],
					  "require_any_tags": ["forgero:base_shape"]
					},
					"secondary": {
					  "explicit_list": ["forgero:oak_handle", "forgero:birch_handle"]
					}
				  }
				}
				""";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertNotNull(config.slots());
		assertEquals(2, config.slots().size());

		SlotGenerationFilter primaryFilter = config.getFilterForSlot("primary");
		assertNotNull(primaryFilter);
		assertNotNull(primaryFilter.requireAllTags());
		assertEquals(2, primaryFilter.requireAllTags().size());
		assertNotNull(primaryFilter.excludeAnyTags());
		assertEquals(2, primaryFilter.excludeAnyTags().size());

		SlotGenerationFilter secondaryFilter = config.getFilterForSlot("secondary");
		assertNotNull(secondaryFilter);
		assertNotNull(secondaryFilter.explicitList());
		assertEquals(2, secondaryFilter.explicitList().size());
	}

	// Edge Case Tests

	@Test
	void testFilterIsEmptyWithEmptyLists() {
		String json = """
				{
				  "require_all_tags": [],
				  "require_any_tags": [],
				  "exclude_any_tags": [],
				  "exclude_all_tags": [],
				  "explicit_list": []
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertTrue(filter.isEmpty(), "Filter with all empty lists should be considered empty");
	}

	@Test
	void testFilterIsNotEmptyWithSingleTag() {
		String json = """
				{
				  "require_all_tags": ["forgero:single_tag"]
				}
				""";

		SlotGenerationFilter filter = parseSuccess(GenerationConfigCodecs.SLOT_GENERATION_FILTER_CODEC, json);
		assertFalse(filter.isEmpty());
	}

	@Test
	void testGenerationConfigWithEmptySlots() {
		String json = """
				{
				  "slots": {}
				}
				""";

		GenerationConfigData config = parseSuccess(GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC, json);
		assertTrue(config.isEmpty(), "Config with empty slots map should be considered empty");
	}
}
