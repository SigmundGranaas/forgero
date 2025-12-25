package com.sigmundgranaas.forgero.data.loading.api.data;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterBuilderTest {

	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	// SlotGenerationFilter.Builder Tests

	@Test
	void testBuilderCreatesEmptyFilter() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder().build();

		assertTrue(filter.isEmpty());
		assertNull(filter.requireAllTags());
		assertNull(filter.requireAnyTags());
		assertNull(filter.excludeAnyTags());
		assertNull(filter.excludeAllTags());
		assertNull(filter.explicitList());
	}

	@Test
	void testBuilderWithRequireAllTags() {
		List<OpenIdentifier> tags = List.of(id("forgero:pickaxe_head_shape"), id("forgero:base_shape"));
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.requireAllTags(tags)
				.build();

		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAllTags());
		assertEquals(2, filter.requireAllTags().size());
		assertTrue(filter.requireAllTags().contains(id("forgero:pickaxe_head_shape")));
		assertTrue(filter.requireAllTags().contains(id("forgero:base_shape")));
	}

	@Test
	void testBuilderWithRequireAnyTags() {
		List<OpenIdentifier> tags = List.of(id("forgero:metal"), id("forgero:wood"));
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.requireAnyTags(tags)
				.build();

		assertNotNull(filter.requireAnyTags());
		assertEquals(2, filter.requireAnyTags().size());
	}

	@Test
	void testBuilderWithExcludeAnyTags() {
		List<OpenIdentifier> tags = List.of(id("forgero:schematic"), id("forgero:cast"));
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.excludeAnyTags(tags)
				.build();

		assertNotNull(filter.excludeAnyTags());
		assertEquals(2, filter.excludeAnyTags().size());
		assertTrue(filter.excludeAnyTags().contains(id("forgero:schematic")));
	}

	@Test
	void testBuilderWithExcludeAllTags() {
		List<OpenIdentifier> tags = List.of(id("forgero:experimental"), id("forgero:disabled"));
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.excludeAllTags(tags)
				.build();

		assertNotNull(filter.excludeAllTags());
		assertEquals(2, filter.excludeAllTags().size());
	}

	@Test
	void testBuilderWithExplicitList() {
		List<OpenIdentifier> ids = List.of(id("forgero:iron"), id("forgero:gold"), id("forgero:diamond"));
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.explicitList(ids)
				.build();

		assertNotNull(filter.explicitList());
		assertEquals(3, filter.explicitList().size());
		assertTrue(filter.explicitList().contains(id("forgero:iron")));
	}

	@Test
	void testBuilderWithMultipleConstraints() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.requireAllTags(List.of(id("forgero:tool_part")))
				.requireAnyTags(List.of(id("forgero:common"), id("forgero:rare")))
				.excludeAnyTags(List.of(id("forgero:deprecated")))
				.build();

		assertFalse(filter.isEmpty());
		assertNotNull(filter.requireAllTags());
		assertNotNull(filter.requireAnyTags());
		assertNotNull(filter.excludeAnyTags());
		assertEquals(1, filter.requireAllTags().size());
		assertEquals(2, filter.requireAnyTags().size());
		assertEquals(1, filter.excludeAnyTags().size());
	}

	@Test
	void testBuilderAddSingleRequireAllTag() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addRequireAllTag(id("forgero:pickaxe_head_shape"))
				.addRequireAllTag(id("forgero:base_shape"))
				.build();

		assertNotNull(filter.requireAllTags());
		assertEquals(2, filter.requireAllTags().size());
		assertTrue(filter.requireAllTags().contains(id("forgero:pickaxe_head_shape")));
		assertTrue(filter.requireAllTags().contains(id("forgero:base_shape")));
	}

	@Test
	void testBuilderAddSingleRequireAnyTag() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addRequireAnyTag(id("forgero:metal"))
				.addRequireAnyTag(id("forgero:wood"))
				.build();

		assertNotNull(filter.requireAnyTags());
		assertEquals(2, filter.requireAnyTags().size());
	}

	@Test
	void testBuilderAddSingleExcludeAnyTag() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addExcludeAnyTag(id("forgero:schematic"))
				.addExcludeAnyTag(id("forgero:cast"))
				.build();

		assertNotNull(filter.excludeAnyTags());
		assertEquals(2, filter.excludeAnyTags().size());
	}

	@Test
	void testBuilderAddSingleExcludeAllTag() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addExcludeAllTag(id("forgero:experimental"))
				.addExcludeAllTag(id("forgero:disabled"))
				.build();

		assertNotNull(filter.excludeAllTags());
		assertEquals(2, filter.excludeAllTags().size());
	}

	@Test
	void testBuilderAddSingleExplicitId() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addExplicitId(id("forgero:iron"))
				.addExplicitId(id("forgero:gold"))
				.addExplicitId(id("forgero:diamond"))
				.build();

		assertNotNull(filter.explicitList());
		assertEquals(3, filter.explicitList().size());
		assertTrue(filter.explicitList().contains(id("forgero:diamond")));
	}

	@Test
	void testBuilderMixingBulkAndSingleAdd() {
		// Use mutable lists for bulk operations
		java.util.List<OpenIdentifier> initialTags = new java.util.ArrayList<>();
		initialTags.add(id("forgero:tool_part"));

		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.requireAllTags(initialTags)
				.addRequireAllTag(id("forgero:base_shape"))
				.addExcludeAnyTag(id("forgero:schematic"))
				.build();

		assertNotNull(filter.requireAllTags());
		assertEquals(2, filter.requireAllTags().size());
		assertNotNull(filter.excludeAnyTags());
		assertEquals(1, filter.excludeAnyTags().size());
	}

	@Test
	void testBuilderFluentInterface() {
		SlotGenerationFilter filter = SlotGenerationFilter.builder()
				.addRequireAllTag(id("forgero:pickaxe_head_shape"))
				.addRequireAllTag(id("forgero:base_shape"))
				.addExcludeAnyTag(id("forgero:schematic"))
				.addExcludeAnyTag(id("forgero:cast"))
				.build();

		assertNotNull(filter.requireAllTags());
		assertEquals(2, filter.requireAllTags().size());
		assertNotNull(filter.excludeAnyTags());
		assertEquals(2, filter.excludeAnyTags().size());
	}

	// GenerationConfigData.Builder Tests

	@Test
	void testConfigBuilderCreatesEmptyConfig() {
		GenerationConfigData config = GenerationConfigData.builder().build();

		assertTrue(config.isEmpty());
		assertNull(config.slots());
	}

	@Test
	void testConfigBuilderWithSingleSlot() {
		SlotGenerationFilter shapeFilter = SlotGenerationFilter.builder()
				.requireAllTags(List.of(id("forgero:pickaxe_head_shape"), id("forgero:base_shape")))
				.build();

		GenerationConfigData config = GenerationConfigData.builder()
				.addSlotFilter("shape", shapeFilter)
				.build();

		assertFalse(config.isEmpty());
		assertNotNull(config.slots());
		assertEquals(1, config.slots().size());
		assertTrue(config.slots().containsKey("shape"));

		SlotGenerationFilter retrievedFilter = config.getFilterForSlot("shape");
		assertNotNull(retrievedFilter);
		assertNotNull(retrievedFilter.requireAllTags());
		assertEquals(2, retrievedFilter.requireAllTags().size());
	}

	@Test
	void testConfigBuilderWithMultipleSlots() {
		SlotGenerationFilter shapeFilter = SlotGenerationFilter.builder()
				.requireAllTags(List.of(id("forgero:pickaxe_head_shape"), id("forgero:base_shape")))
				.build();

		SlotGenerationFilter materialFilter = SlotGenerationFilter.builder()
				.requireAnyTags(List.of(id("forgero:metal"), id("forgero:gem")))
				.build();

		GenerationConfigData config = GenerationConfigData.builder()
				.addSlotFilter("shape", shapeFilter)
				.addSlotFilter("material", materialFilter)
				.build();

		assertNotNull(config.slots());
		assertEquals(2, config.slots().size());
		assertNotNull(config.getFilterForSlot("shape"));
		assertNotNull(config.getFilterForSlot("material"));
	}

	@Test
	void testConfigBuilderRemoveSlot() {
		SlotGenerationFilter shapeFilter = SlotGenerationFilter.builder()
				.requireAllTags(List.of(id("forgero:pickaxe_head_shape")))
				.build();

		GenerationConfigData config = GenerationConfigData.builder()
				.addSlotFilter("shape", shapeFilter)
				.addSlotFilter("material", SlotGenerationFilter.builder().build())
				.removeSlotFilter("material")
				.build();

		assertNotNull(config.slots());
		assertEquals(1, config.slots().size());
		assertTrue(config.slots().containsKey("shape"));
		assertFalse(config.slots().containsKey("material"));
	}

	@Test
	void testConfigBuilderWithCompleteMap() {
		SlotGenerationFilter shapeFilter = SlotGenerationFilter.builder()
				.requireAllTags(List.of(id("forgero:pickaxe_head_shape")))
				.build();

		SlotGenerationFilter materialFilter = SlotGenerationFilter.builder()
				.requireAnyTags(List.of(id("forgero:metal")))
				.build();

		java.util.Map<String, SlotGenerationFilter> slotMap = new java.util.HashMap<>();
		slotMap.put("shape", shapeFilter);
		slotMap.put("material", materialFilter);

		GenerationConfigData config = GenerationConfigData.builder()
				.slots(slotMap)
				.build();

		assertNotNull(config.slots());
		assertEquals(2, config.slots().size());
	}

	@Test
	void testConfigBuilderFluentInterface() {
		GenerationConfigData config = GenerationConfigData.builder()
				.addSlotFilter("primary", SlotGenerationFilter.builder()
						.addRequireAllTag(id("forgero:primary_part"))
						.addExcludeAnyTag(id("forgero:schematic"))
						.build())
				.addSlotFilter("secondary", SlotGenerationFilter.builder()
						.addExplicitId(id("forgero:oak_handle"))
						.addExplicitId(id("forgero:birch_handle"))
						.build())
				.build();

		assertNotNull(config.slots());
		assertEquals(2, config.slots().size());

		SlotGenerationFilter primaryFilter = config.getFilterForSlot("primary");
		assertNotNull(primaryFilter);
		assertNotNull(primaryFilter.requireAllTags());
		assertNotNull(primaryFilter.excludeAnyTags());

		SlotGenerationFilter secondaryFilter = config.getFilterForSlot("secondary");
		assertNotNull(secondaryFilter);
		assertNotNull(secondaryFilter.explicitList());
		assertEquals(2, secondaryFilter.explicitList().size());
	}

	@Test
	void testConfigBuilderOverwriteSlot() {
		SlotGenerationFilter filter1 = SlotGenerationFilter.builder()
				.addRequireAllTag(id("forgero:tag1"))
				.build();

		SlotGenerationFilter filter2 = SlotGenerationFilter.builder()
				.addRequireAllTag(id("forgero:tag2"))
				.build();

		GenerationConfigData config = GenerationConfigData.builder()
				.addSlotFilter("shape", filter1)
				.addSlotFilter("shape", filter2) // Overwrites the previous filter
				.build();

		assertNotNull(config.slots());
		assertEquals(1, config.slots().size());

		SlotGenerationFilter retrievedFilter = config.getFilterForSlot("shape");
		assertNotNull(retrievedFilter.requireAllTags());
		assertTrue(retrievedFilter.requireAllTags().contains(id("forgero:tag2")));
		assertFalse(retrievedFilter.requireAllTags().contains(id("forgero:tag1")));
	}
}
