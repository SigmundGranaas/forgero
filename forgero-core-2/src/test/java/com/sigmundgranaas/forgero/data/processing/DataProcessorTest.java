package com.sigmundgranaas.forgero.data.processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.Utils;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningSelectorData;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import com.sigmundgranaas.forgero.data.processing.impl.DataProcessorImpl;
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
		processor = new DataProcessorImpl();
	}

	@Test
	void testConsolidatesAllPropertiesIntoSingleMap() {
		AttributeData attr = createAttribute("attr1", "forgero:damage", 10f);
		FeatureData feature = createVeinMiningFeature("forgero:vein_mining");
		Map<String, JsonElement> customProps = Map.of("custom:prop", new JsonPrimitive(true));

		RawDefinition raw = createRawMaterial("test:data1", "Data 1", null, null, List.of(attr), List.of(feature), customProps);
		Map<OpenIdentifier, RawDefinition> rawData = Map.of(raw.id(), raw);

		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(raw.id());

		assertNotNull(result.properties(), "Properties map should not be null.");
		assertEquals(3, result.properties().size(), "Should have 3 entries: attributes, features, and custom prop.");

		assertTrue(result.properties().containsKey(ATTRIBUTES_KEY), "Should contain consolidated attributes.");
		assertTrue(result.properties().get(ATTRIBUTES_KEY).isJsonArray(), "Attributes should be a JSON array.");
		assertEquals(1, result.properties().get(ATTRIBUTES_KEY).getAsJsonArray().size());

		assertTrue(result.properties().containsKey(FEATURES_KEY), "Should contain consolidated features.");
		assertTrue(result.properties().get(FEATURES_KEY).isJsonArray(), "Features should be a JSON array.");
		assertEquals(1, result.properties().get(FEATURES_KEY).getAsJsonArray().size());

		assertTrue(result.properties().containsKey("custom:prop"), "Custom property should be present.");
		assertTrue(result.properties().get("custom:prop").getAsBoolean());
	}

	@Test
	void testIncludeMergesAndOverwritesPropertiesCorrectly() {
		// Included definition
		AttributeData includedAttr = createAttribute("included_attr", "forgero:durability", 50f);
		Map<String, JsonElement> includedProps = Map.of("custom:shared_prop", new JsonPrimitive("from_include"));
		RawDefinition included = createRawMaterial("test:included", "Included", null, null, List.of(includedAttr), null, includedProps);

		// Base definition
		AttributeData baseAttr1 = createAttribute("base_attr", "forgero:speed", 5f);
		AttributeData baseAttr2 = createAttribute("included_attr", "forgero:durability", 100f); // Overrides included
		Map<String, JsonElement> baseProps = Map.of("custom:shared_prop", new JsonPrimitive("from_base"));
		RawDefinition base = createRawMaterial("test:base", "Base", List.of("test:included"), null, List.of(baseAttr1, baseAttr2), null, baseProps);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(included.id(), included, base.id(), base);
		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(base.id());

		assertNotNull(result.properties());

		// Assert custom property was overridden
		assertTrue(result.properties().containsKey("custom:shared_prop"));
		assertEquals("from_base", result.properties().get("custom:shared_prop").getAsString());

		// Assert attributes were merged and overridden
		assertTrue(result.properties().containsKey(ATTRIBUTES_KEY));
		JsonArray attributesJson = result.properties().get(ATTRIBUTES_KEY).getAsJsonArray();
		assertEquals(2, attributesJson.size(), "Should have two unique attributes after merging.");

		// Verify the overridden attribute has the correct value
		long matchingDurabilityCount = attributesJson.asList().stream()
				.map(JsonElement::getAsJsonObject)
				.filter(obj -> obj.get("id").getAsString().equals("forgero:included_attr"))
				// Access the nested 'value' inside the 'computation' object
				.filter(obj -> obj.get("computation").getAsJsonObject().get("value").getAsFloat() == 100f)
				.count();
		assertEquals(1, matchingDurabilityCount, "The overridden durability attribute should have the value from the base definition.");

		// Verify the unique base attribute is present
		boolean hasSpeedAttribute = attributesJson.asList().stream()
				.map(JsonElement::getAsJsonObject)
				.anyMatch(obj -> obj.get("id").getAsString().equals("forgero:base_attr"));
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
