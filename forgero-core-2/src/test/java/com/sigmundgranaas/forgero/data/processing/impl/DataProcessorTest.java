package com.sigmundgranaas.forgero.data.processing.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.data.Utils;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningSelectorData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest extends ForgeroTest {

	private DataProcessor processor;

	private static final String ATTRIBUTES_KEY = "forgero:attributes";
	private static final String FEATURES_KEY = "forgero:features";

	@BeforeEach
	void setUp() {
		// Ensure property codecs are registered for the test environment
		com.sigmundgranaas.forgero.core.property.api.PropertyRegistry.getInstance().reset();
		com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs.VEIN_MINING_FEATURE_CODEC.toString(); // Initialize static block
		processor = new DataProcessorImpl();
	}

	@Test
	void testConsolidatesAllPropertiesIntoSingleMap() {
		AttributeData attr = createAttribute("attr1", "forgero:damage", 10f);
		FeatureData feature = createVeinMiningFeature("forgero:vein_mining");

		RawDefinition raw = createRawMaterial("test:data1", "Data 1", null, null, List.of(attr), List.of(feature), null);
		Map<OpenIdentifier, RawDefinition> rawData = Map.of(raw.id(), raw);

		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(raw.id());

		assertNotNull(result.properties(), "Properties map should not be null.");
		assertEquals(2, result.properties().size(), "Should have 2 entries: attributes and features.");

		assertTrue(result.properties().containsKey(ATTRIBUTES_KEY), "Should contain consolidated attributes.");
		List<PropertyData> attributes = result.properties().get(ATTRIBUTES_KEY);
		assertEquals(1, attributes.size());

		assertTrue(result.properties().containsKey(FEATURES_KEY), "Should contain consolidated features.");
		List<PropertyData> features = result.properties().get(FEATURES_KEY);
		assertEquals(1, features.size());
	}

	@Test
	void testIncludeMergesAndOverwritesPropertiesCorrectly() {
		// Included definition
		AttributeData includedAttr = createAttribute("included_attr", "forgero:durability", 50f);
		RawDefinition included = createRawMaterial("test:included", "Included", null, null, List.of(includedAttr), null, null);

		// Base definition
		AttributeData baseAttr1 = createAttribute("base_attr", "forgero:speed", 5f);
		AttributeData baseAttr2 = createAttribute("included_attr", "forgero:durability", 100f); // Overrides included
		RawDefinition base = createRawMaterial("test:base", "Base", List.of("test:included"), null, List.of(baseAttr1, baseAttr2), null, null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(included.id(), included, base.id(), base);
		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(base.id());

		assertNotNull(result.properties());
		assertTrue(result.properties().containsKey(ATTRIBUTES_KEY));

		List<AttributeData> attributes = result.properties().get(ATTRIBUTES_KEY).stream()
				.map(AttributeData.class::cast)
				.toList();
		assertEquals(2, attributes.size(), "Should have two unique attributes after merging.");


		// Verify the overridden attribute has the correct value
		long matchingDurabilityCount = attributes.stream()
				.filter(attr -> attr.id().toString().equals("forgero:included_attr"))
				.filter(attr -> attr.computation().value() == 100f)
				.count();
		assertEquals(1, matchingDurabilityCount, "The overridden durability attribute should have the value from the base definition.");

		// Verify the unique base attribute is present
		boolean hasSpeedAttribute = attributes.stream()
				.anyMatch(attr -> attr.id().toString().equals("forgero:base_attr"));
		assertTrue(hasSpeedAttribute, "The unique attribute from the base definition should be present.");
	}

	@Test
	void testCyclicIncludeThrowsError() {
		RawDefinition a = createRawMaterial("test:a", "A", List.of("test:b"), null, null, null, null);
		RawDefinition b = createRawMaterial("test:b", "B", List.of("test:a"), null, null, null, null);
		Map<OpenIdentifier, RawDefinition> rawData = Map.of(a.id(), a, b.id(), b);

		assertThrows(IllegalStateException.class, () -> processor.normalize(rawData), "Cyclic dependency should cause an exception.");
	}


	// region Test Data Factories
	private RawDefinition createRawMaterial(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features, @Nullable Map<String, JsonElement> properties) {
		MaterialData dto = new MaterialData(id("forgero:material"), name,
				include == null ? null : include.stream().map(Utils::id).toList(),
				tags == null ? null : tags.stream().map(Utils::id).toList(),
				new HostData(List.of(new IdentifierEntry("id",id("forgero:material"))),null),
				attributes, features, properties);
		return new RawDefinition(id(idPath), dto);
	}

	private AttributeData createAttribute(String id, String type, float value) {
		return new AttributeDataImpl(id(id), id(type), new ComputationData(value, "forgero:addition", "forgero:base"), null, null);
	}

	private FeatureData createVeinMiningFeature(String type) {
		return new VeinMiningFeatureData(id(type), "Vein Mining", "Mines veins", new VeinMiningSelectorData(id("forgero:radius"), 1, id("forgero:ores")), null);
	}
	// endregion
}
