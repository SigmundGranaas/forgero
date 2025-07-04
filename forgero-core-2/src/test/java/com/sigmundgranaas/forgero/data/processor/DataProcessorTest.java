package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningSelectorData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest extends ForgeroTest {

	private DataProcessor processor;

	// --- Mock DTOs for testing the processor logic ---
	// These are simplified records implementing the interfaces needed by the processor.

	// MockAttributeData now correctly implements the AttributeData interface
	record MockAttributeData(OpenIdentifier id, OpenIdentifier type, ComputationData computation,
							 @Nullable ConditionData condition, @Nullable OpenIdentifier composite) implements AttributeData {
		// Constructor for simplicity
		MockAttributeData(String idPath, String typePath, float value) {
			this(idFactory.of(idPath), idFactory.of(typePath), new ComputationData(value, "forgero:addition", "forgero:base"), null, null);
		}
		MockAttributeData(String idPath, String typePath, float value, String operator) {
			this(idFactory.of(idPath), idFactory.of(typePath), new ComputationData(value, operator, "forgero:base"), null, null);
		}
	}

	record MockFeatureData(OpenIdentifier type, String title, String description,
						   VeinMiningSelectorData selector, @Nullable ConditionData condition) implements FeatureData {
		MockFeatureData(String typePath, String title, String description) {
			this(idFactory.of(typePath), title, description, null, null);
		}
	}

	// A generic mock for any TopLevelData type for the processor to operate on
	record MockTopLevelData(
			OpenIdentifier id,
			OpenIdentifier type,
			String name,
			@Nullable List<OpenIdentifier> include,
			@Nullable Set<OpenIdentifier> tags,
			@Nullable List<AttributeData> attributes,
			@Nullable List<FeatureData> features
	) implements TopLevelData {
		// Helper constructor for simpler test data setup
		MockTopLevelData(String idPath, String name, @Nullable List<OpenIdentifier> include, @Nullable Set<OpenIdentifier> tags,
						 @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
			this(idFactory.of(idPath), idFactory.of("forgero:test_type"), name, include, tags, attributes, features);
		}

		// Override equals and hashCode for reliable map behavior in tests, focusing on `id`
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MockTopLevelData that = (MockTopLevelData) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public <T> T unwrapAs(@NotNull Class<T> type) {
			return null;
		}
	}
	// --- End Mock DTOs ---

	@BeforeEach
	void setUp() {
		processor = new DataProcessorImpl();
	}

	@Test
	void testProcess_NoIncludes() {
		MockTopLevelData data1 = new MockTopLevelData(
				"test:data1", "Data 1",
				null, Set.of(idFactory.of("tag1")),
				List.of(new MockAttributeData("attr1", "damage", 10f)),
				List.of(new MockFeatureData("feature1", "F1", "Desc1"))
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
		MockTopLevelData includedData = new MockTopLevelData(
				"test:included", "Included",
				null, Set.of(idFactory.of("inc_tag")),
				List.of(new MockAttributeData("inc_attr1", "durability", 50f)),
				List.of(new MockFeatureData("inc_feature1", "IncF1", "IncDesc1"))
		);

		MockTopLevelData baseData = new MockTopLevelData(
				"test:base", "Base",
				List.of(includedData.id()),
				Set.of(idFactory.of("base_tag")),
				List.of(new MockAttributeData("base_attr1", "speed", 5f)),
				List.of(new MockFeatureData("base_feature1", "BaseF1", "BaseDesc1"))
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
		MockAttributeData includedAttr = new MockAttributeData("shared_attr", "damage", 100f);
		MockTopLevelData includedData = new MockTopLevelData(
				"test:included", "Included", null, null, List.of(includedAttr), null
		);

		MockAttributeData baseAttr = new MockAttributeData("shared_attr", "damage", 200f); // Higher value
		MockTopLevelData baseData = new MockTopLevelData(
				"test:base", "Base",
				List.of(includedData.id()), null, List.of(baseAttr), null
		);

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
		MockFeatureData includedFeature = new MockFeatureData("shared_feature_type", "IncF", "IncDesc");
		MockTopLevelData includedData = new MockTopLevelData(
				"test:included", "Included", null, null, null, List.of(includedFeature)
		);

		MockFeatureData baseFeature = new MockFeatureData("shared_feature_type", "BaseF", "BaseDesc"); // Base overrides
		MockTopLevelData baseData = new MockTopLevelData(
				"test:base", "Base",
				List.of(includedData.id()), null, null, List.of(baseFeature)
		);

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
	}

	@Test
	void testProcess_MultipleIncludes_OrderPrecedence() {
		// inc1 declares attr: value 10
		MockTopLevelData inc1 = new MockTopLevelData("test:inc1", "inc1", null, null, List.of(new MockAttributeData("attr_a", "type", 10f)), null);
		// inc2 declares attr: value 20 (should override inc1)
		MockTopLevelData inc2 = new MockTopLevelData("test:inc2", "inc2", null, null, List.of(new MockAttributeData("attr_a", "type", 20f)), null);
		// base declares attr: value 30 (should override inc2)
		MockTopLevelData base = new MockTopLevelData("test:base", "base", List.of(inc1.id(), inc2.id()), null, List.of(new MockAttributeData("attr_a", "type", 30f)), null);

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
		// C includes nothing, provides attr: c_attr
		MockTopLevelData C = new MockTopLevelData("test:c", "C", null, Set.of(idFactory.of("tag_c")), List.of(new MockAttributeData("attr_c", "type", 30f)), null);
		// B includes C, provides attr: b_attr
		MockTopLevelData B = new MockTopLevelData("test:b", "B", List.of(C.id()), Set.of(idFactory.of("tag_b")), List.of(new MockAttributeData("attr_b", "type", 20f)), null);
		// A includes B, provides attr: a_attr
		MockTopLevelData A = new MockTopLevelData("test:a", "A", List.of(B.id()), Set.of(idFactory.of("tag_a")), List.of(new MockAttributeData("attr_a", "type", 10f)), null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B, C.id(), C);
		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData resultA = processed.get(A.id());

		assertNull(resultA.include(), "Include list should be null after processing");

		// Verify A contains properties from A, B, and C
		assertEquals(3, resultA.tags().size());
		assertTrue(resultA.tags().containsAll(List.of(idFactory.of("tag_a"), idFactory.of("tag_b"), idFactory.of("tag_c"))));

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
		MockTopLevelData baseData = new MockTopLevelData("test:base", "Base", List.of(idFactory.of("test:non_existent_id")), null, null, null);
		Map<OpenIdentifier, TopLevelData> rawData = Map.of(baseData.id(), baseData);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Included definition not found: test:non_existent_id"));
	}

	@Test
	void testProcess_CyclicInclude_Direct() {
		MockTopLevelData A = new MockTopLevelData("test:a", "A", List.of(idFactory.of("test:b")), null, null, null);
		MockTopLevelData B = new MockTopLevelData("test:b", "B", List.of(idFactory.of("test:a")), null, null, null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected involving: test:a") ||
				thrown.getMessage().contains("Cyclic include dependency detected involving: test:b"));
	}

	@Test
	void testProcess_CyclicInclude_Transitive() {
		MockTopLevelData A = new MockTopLevelData("test:a", "A", List.of(idFactory.of("test:b")), null, null, null);
		MockTopLevelData B = new MockTopLevelData("test:b", "B", List.of(idFactory.of("test:c")), null, null, null);
		MockTopLevelData C = new MockTopLevelData("test:c", "C", List.of(idFactory.of("test:a")), null, null, null);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(A.id(), A, B.id(), B, C.id(), C);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.process(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected"));
	}

	@Test
	void testProcess_IncludesDoNotInheritIncludesList() {
		MockTopLevelData includedData = new MockTopLevelData(
				"test:included", "Included",
				List.of(idFactory.of("test:another_included")), null, null, null
		);
		MockTopLevelData anotherIncludedData = new MockTopLevelData(
				"test:another_included", "Another Included", null, null, null, null
		);

		MockTopLevelData baseData = new MockTopLevelData(
				"test:base", "Base",
				List.of(includedData.id()), null, null, null
		);

		Map<OpenIdentifier, TopLevelData> rawData = Map.of(
				includedData.id(), includedData,
				anotherIncludedData.id(), anotherIncludedData,
				baseData.id(), baseData
		);

		Map<OpenIdentifier, TopLevelData> processed = processor.process(rawData);
		TopLevelData result = processed.get(baseData.id());

		// The 'include' list of the *result* object should be null, as includes are a processing instruction, not an inherent property.
		assertNull(result.include(), "The include list of the result should be null as it's been processed.");

		// To verify that the includes were correctly processed, we need to check the attributes/tags/features
		// For this mock, we can add a simple property to another_included
		MockTopLevelData anotherIncludedDataWithTag = new MockTopLevelData(
				"test:another_included", "Another Included", null, Set.of(idFactory.of("another_tag")), null, null
		);
		rawData = Map.of(
				includedData.id(), new MockTopLevelData("test:included", "Included", List.of(anotherIncludedDataWithTag.id()), null, null, null),
				anotherIncludedDataWithTag.id(), anotherIncludedDataWithTag,
				baseData.id(), new MockTopLevelData("test:base", "Base", List.of(includedData.id()), null, null, null)
		);
		processed = processor.process(rawData);
		result = processed.get(baseData.id());

		// The base data should now contain the tag from `another_included` via `includedData`
		assertNotNull(result.tags());
		assertTrue(result.tags().contains(idFactory.of("another_tag")), "Tag from nested include should be present");
		assertNull(result.include(), "After processing, the include list should be cleared as it's no longer needed for merge state.");
	}
}
