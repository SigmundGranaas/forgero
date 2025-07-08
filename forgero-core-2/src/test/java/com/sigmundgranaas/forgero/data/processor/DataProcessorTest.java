package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.IdentifiedTopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest extends ForgeroTest {

	private DataProcessor processor;

	// Helper to create test data using real DTOs, which is more robust than mocks.
	private TopLevelData createTestData(String idPath, String name, @Nullable List<OpenIdentifier> include, @Nullable List<OpenIdentifier> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
		StaticPartData dto = new StaticPartData(
				idFactory.of("forgero:static_part"), // A consistent type for the DTO
				name,
				include,
				tags,
				attributes,
				features
		);
		return new IdentifiedTopLevelData(idFactory.of(idPath), dto);
	}

	@BeforeEach
	void setUp() {
		processor = new DataProcessorImpl();
	}

	@Test
	void testProcess_NoIncludes() {
		AttributeData attr = new AttributeDataImpl(idFactory.of("attr1"), idFactory.of("damage"), new ComputationData(10f, "forgero:addition", "forgero:base"), null, null);
		FeatureData feature = new VeinMiningFeatureData(idFactory.of("feature1"), "F1", "Desc1", null, null);

		TopLevelData data1 = createTestData(
				"test:data1", "Data 1",
				null, List.of(idFactory.of("tag1")),
				List.of(attr),
				List.of(feature)
		);
		Map<OpenIdentifier, TopLevelData> rawData = Map.of(data1.id(), data1);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);

		assertEquals(1, processed.size());
		assertTrue(processed.containsKey(data1.id()));
		TopLevelData result = processed.get(data1.id());
		assertNotNull(result);
		assertEquals(data1.id(), result.id());
		assertEquals(data1.name(), result.name());
		assertNull(result.include()); // Should now be null after processing
		assertEquals(1, result.tags().size());
		assertEquals(1, result.attributes().size());
		assertEquals(1, result.features().size());
	}

	@Test
	void testProcess_SingleInclude_NoConflict() {
		AttributeData includedAttr = new AttributeDataImpl(idFactory.of("inc_attr1"), idFactory.of("durability"), new ComputationData(50f, "forgero:addition", "forgero:base"), null, null);
		FeatureData includedFeature = new VeinMiningFeatureData(idFactory.of("inc_feature1"), "IncF1", "IncDesc1", null, null);
		TopLevelData includedData = createTestData(
				"test:included", "Included",
				null, List.of(idFactory.of("inc_tag")), List.of(includedAttr), List.of(includedFeature)
		);

		AttributeData baseAttr = new AttributeDataImpl(idFactory.of("base_attr1"), idFactory.of("speed"), new ComputationData(5f, "forgero:addition", "forgero:base"), null, null);
		FeatureData baseFeature = new VeinMiningFeatureData(idFactory.of("base_feature1"), "BaseF1", "BaseDesc1", null, null);
		TopLevelData baseData = createTestData(
				"test:base", "Base",
				List.of(includedData.id()), List.of(idFactory.of("base_tag")), List.of(baseAttr), List.of(baseFeature)
		);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				includedData.id(), includedData,
				baseData.id(), baseData
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);

		assertTrue(processed.containsKey(baseData.id()));
		TopLevelData result = processed.get(baseData.id());

		assertNull(result.include(), "Include list should be null after processing");

		// Tags should merge
		assertEquals(2, result.tags().size());
		assertTrue(result.tags().contains(idFactory.of("inc_tag")));
		assertTrue(result.tags().contains(idFactory.of("base_tag")));

		// Attributes should merge
		assertEquals(2, result.attributes().size());
		assertTrue(result.getAttributesMap().containsKey(idFactory.of("inc_attr1")));
		assertTrue(result.getAttributesMap().containsKey(idFactory.of("base_attr1")));
		assertEquals(50f, result.getAttributesMap().get(idFactory.of("inc_attr1")).computation().value());
		assertEquals(5f, result.getAttributesMap().get(idFactory.of("base_attr1")).computation().value());

		// Features should merge
		assertEquals(2, result.features().size());
		assertTrue(result.getFeaturesMap().containsKey(idFactory.of("inc_feature1")));
		assertTrue(result.getFeaturesMap().containsKey(idFactory.of("base_feature1")));
	}

	@Test
	void testProcess_SingleInclude_AttributeConflict() {
		AttributeData includedAttr = new AttributeDataImpl(idFactory.of("shared_attr"), idFactory.of("damage"), new ComputationData(100f, "forgero:addition", "forgero:base"), null, null);
		TopLevelData includedData = createTestData("test:included", "Included", null, null, List.of(includedAttr), null);

		AttributeData baseAttr = new AttributeDataImpl(idFactory.of("shared_attr"), idFactory.of("damage"), new ComputationData(200f, "forgero:addition", "forgero:base"), null, null);
		TopLevelData baseData = createTestData("test:base", "Base", List.of(includedData.id()), null, List.of(baseAttr), null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				includedData.id(), includedData,
				baseData.id(), baseData
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData result = processed.get(baseData.id());

		assertNull(result.include(), "Include list should be null after processing");
		assertEquals(1, result.attributes().size());
		AttributeData finalAttr = result.attributes().get(0);
		assertEquals(idFactory.of("shared_attr"), finalAttr.id());
		assertEquals(200f, finalAttr.computation().value(), "Base attribute should override included one.");
	}

	@Test
	void testProcess_SingleInclude_FeatureConflict() {
		FeatureData includedFeature = new VeinMiningFeatureData(idFactory.of("shared_feature_type"), "IncF", "IncDesc", null, null);
		TopLevelData includedData = createTestData("test:included", "Included", null, null, null, List.of(includedFeature));

		FeatureData baseFeature = new VeinMiningFeatureData(idFactory.of("shared_feature_type"), "BaseF", "BaseDesc", null, null);
		TopLevelData baseData = createTestData("test:base", "Base", List.of(includedData.id()), null, null, List.of(baseFeature));

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				includedData.id(), includedData,
				baseData.id(), baseData
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData result = processed.get(baseData.id());

		assertNull(result.include(), "Include list should be null after processing");
		assertEquals(1, result.features().size());
		FeatureData finalFeature = result.features().get(0);
		assertEquals(idFactory.of("shared_feature_type"), finalFeature.type());
		assertEquals("BaseF", ((VeinMiningFeatureData) finalFeature).title(), "Base feature should override included one.");
	}

	@Test
	void testProcess_MultipleIncludes_OrderPrecedence() {
		TopLevelData inc1 = createTestData("test:inc1", "inc1", null, null, List.of(new AttributeDataImpl(idFactory.of("attr_a"), idFactory.of("type"), new ComputationData(10f, "forgero:addition", "forgero:base"), null, null)), null);
		TopLevelData inc2 = createTestData("test:inc2", "inc2", null, null, List.of(new AttributeDataImpl(idFactory.of("attr_a"), idFactory.of("type"), new ComputationData(20f, "forgero:addition", "forgero:base"), null, null)), null);
		TopLevelData base = createTestData("test:base", "base", List.of(inc1.id(), inc2.id()), null, List.of(new AttributeDataImpl(idFactory.of("attr_a"), idFactory.of("type"), new ComputationData(30f, "forgero:addition", "forgero:base"), null, null)), null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				inc1.id(), inc1,
				inc2.id(), inc2,
				base.id(), base
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData result = processed.get(base.id());

		assertNull(result.include(), "Include list should be null after processing");
		assertEquals(1, result.attributes().size());
		assertEquals(30f, result.attributes().get(0).computation().value(), "Base data should override all includes.");
	}

	@Test
	void testProcess_NestedIncludes() {
		TopLevelData C = createTestData("test:c", "C", null, List.of(idFactory.of("tag_c")), List.of(new AttributeDataImpl(idFactory.of("attr_c"), idFactory.of("type"), new ComputationData(30f, "forgero:addition", "forgero:base"), null, null)), null);
		TopLevelData B = createTestData("test:b", "B", List.of(C.id()), List.of(idFactory.of("tag_b")), List.of(new AttributeDataImpl(idFactory.of("attr_b"), idFactory.of("type"), new ComputationData(20f, "forgero:addition", "forgero:base"), null, null)), null);
		TopLevelData A = createTestData("test:a", "A", List.of(B.id()), List.of(idFactory.of("tag_a")), List.of(new AttributeDataImpl(idFactory.of("attr_a"), idFactory.of("type"), new ComputationData(10f, "forgero:addition", "forgero:base"), null, null)), null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B, C.id(), C);
		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData resultA = processed.get(A.id());

		assertNull(resultA.include(), "Include list should be null after processing");

		assertEquals(3, resultA.tags().size());
		assertTrue(Objects.requireNonNull(resultA.tags()).containsAll(List.of(idFactory.of("tag_a"), idFactory.of("tag_b"), idFactory.of("tag_c"))));

		assertEquals(3, resultA.attributes().size());
		Map<OpenIdentifier, AttributeData> attrs = resultA.getAttributesMap();
		assertTrue(attrs.containsKey(idFactory.of("attr_a")));
		assertTrue(attrs.containsKey(idFactory.of("attr_b")));
		assertTrue(attrs.containsKey(idFactory.of("attr_c")));
		assertEquals(10f, attrs.get(idFactory.of("attr_a")).computation().value());
		assertEquals(20f, attrs.get(idFactory.of("attr_b")).computation().value());
		assertEquals(30f, attrs.get(idFactory.of("attr_c")).computation().value());
	}

	@Test
	void testProcess_NonExistentInclude() {
		TopLevelData baseData = createTestData("test:base", "Base", List.of(idFactory.of("test:non_existent_id")), null, null, null);
		Map<OpenIdentifier, TopLevelData> rawData = Map.of(baseData.id(), baseData);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Included definition not found: test:non_existent_id"));
	}

	@Test
	void testProcess_CyclicInclude_Direct() {
		TopLevelData A = createTestData("test:a", "A", List.of(idFactory.of("test:b")), null, null, null);
		TopLevelData B = createTestData("test:b", "B", List.of(idFactory.of("test:a")), null, null, null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected involving: test:a") ||
				thrown.getMessage().contains("Cyclic include dependency detected involving: test:b"));
	}

	@Test
	void testProcess_CyclicInclude_Transitive() {
		TopLevelData A = createTestData("test:a", "A", List.of(idFactory.of("test:b")), null, null, null);
		TopLevelData B = createTestData("test:b", "B", List.of(idFactory.of("test:c")), null, null, null);
		TopLevelData C = createTestData("test:c", "C", List.of(idFactory.of("test:a")), null, null, null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B, C.id(), C);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected"));
	}

	@Test
	void testProcess_IncludesDoNotInheritIncludesList() {
		TopLevelData anotherIncludedData = createTestData("test:another_included", "Another Included", null, List.of(idFactory.of("another_tag")), null, null);
		TopLevelData includedData = createTestData("test:included", "Included", List.of(anotherIncludedData.id()), null, null, null);
		TopLevelData baseData = createTestData("test:base", "Base", List.of(includedData.id()), null, null, null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				includedData.id(), includedData,
				anotherIncludedData.id(), anotherIncludedData,
				baseData.id(), baseData
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData result = processed.get(baseData.id());

		assertNull(result.include(), "The include list of the result should be null as it's been processed.");
		assertNotNull(result.tags());
		assertTrue(result.tags().contains(idFactory.of("another_tag")), "Tag from nested include should be present");
	}
}
