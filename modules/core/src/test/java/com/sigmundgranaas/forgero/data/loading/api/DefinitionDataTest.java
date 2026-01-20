package com.sigmundgranaas.forgero.data.loading.api;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefinitionDataTest {

	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	@Nested
	class ResourceDataImplementation {

		@Test
		void shouldImplementDefinitionData() {
			ResourceData data = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null, null
			);

			assertTrue(data instanceof DefinitionData);
		}

		@Test
		void withMergedExtension_shouldReturnNewInstance() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null, null, null, null, null
			);

			DefinitionData merged = original.withMergedExtension(
					List.of(id("forgero:metal"), id("forgero:tool")),
					List.of(),
					Map.of()
			);

			assertNotSame(original, merged);
			assertTrue(merged instanceof ResourceData);
		}

		@Test
		void withMergedExtension_shouldPreserveName() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null, null
			);

			DefinitionData merged = original.withMergedExtension(
					List.of(id("forgero:new_tag")),
					List.of(),
					Map.of()
			);

			assertEquals("iron", merged.name());
		}

		@Test
		void withMergedExtension_shouldPreserveType() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null, null
			);

			DefinitionData merged = original.withMergedExtension(
					List.of(),
					List.of(),
					Map.of()
			);

			assertEquals(id("forgero:material"), merged.type());
		}

		@Test
		void withMergedExtension_shouldApplyMergedTags() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:base")),
					null, null, null, null, null
			);

			List<OpenIdentifier> newTags = List.of(id("forgero:merged1"), id("forgero:merged2"));
			DefinitionData merged = original.withMergedExtension(newTags, List.of(), Map.of());

			assertEquals(2, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:merged1")));
			assertTrue(merged.tags().contains(id("forgero:merged2")));
		}
	}

	@Nested
	class ExtensionDataImplementation {

		@Test
		void shouldImplementDefinitionData() {
			ExtensionData data = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null, null
			);

			assertTrue(data instanceof DefinitionData);
		}

		@Test
		void withMergedExtension_shouldReturnSelf() {
			ExtensionData original = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tag")),
					null, null
			);

			DefinitionData merged = original.withMergedExtension(
					List.of(id("forgero:new_tag")),
					List.of(),
					Map.of()
			);

			// Extensions don't support being merged into
			assertSame(original, merged);
		}

		@Test
		void include_shouldReturnEmptyList() {
			ExtensionData data = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null, null
			);

			assertNotNull(data.include());
			assertTrue(data.include().isEmpty());
		}

		@Test
		void name_shouldReturnTargetName() {
			ExtensionData data = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null, null
			);

			assertEquals("iron", data.name());
		}
	}

	@Nested
	class InterfaceContract {

		@Test
		void allImplementations_shouldHaveType() {
			ResourceData resource = new ResourceData(
					id("forgero:material"),
					"test",
					null, null, null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/test"),
					0,
					null, null, null
			);

			assertNotNull(resource.type());
			assertNotNull(extension.type());
		}

		@Test
		void allImplementations_shouldHaveName() {
			ResourceData resource = new ResourceData(
					id("forgero:material"),
					"test_name",
					null, null, null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/target"),
					0,
					null, null, null
			);

			assertNotNull(resource.name());
			assertNotNull(extension.name());
		}

		@Test
		void nullFields_shouldBeHandledGracefully() {
			ResourceData resource = new ResourceData(
					id("forgero:material"),
					"test",
					null, null, null, null, null, null, null
			);

			// Should not throw
			assertNull(resource.include());
			assertNull(resource.tags());
			assertNull(resource.attributes());
			assertNull(resource.properties());
		}
	}

	@Nested
	class FactoryMethods {

		@Test
		void fromMaterialData_shouldCreateEquivalentResourceData() {
			com.sigmundgranaas.forgero.data.loading.api.data.MaterialData material =
					new com.sigmundgranaas.forgero.data.loading.api.data.MaterialData(
							id("forgero:material"),
							"iron",
							null,
							List.of(id("forgero:metal")),
							null, null, null, null, null
					);

			ResourceData converted = ResourceData.from(material);

			assertEquals(material.type(), converted.type());
			assertEquals(material.name(), converted.name());
			assertEquals(material.tags(), converted.tags());
		}

		@Test
		void fromShapeData_shouldCreateEquivalentResourceData() {
			com.sigmundgranaas.forgero.data.loading.api.data.ShapeData shape =
					new com.sigmundgranaas.forgero.data.loading.api.data.ShapeData(
							id("forgero:shape"),
							"pickaxe_head",
							null,
							List.of(id("forgero:tool_shape")),
							null, null, null, null, null
					);

			ResourceData converted = ResourceData.from(shape);

			assertEquals(shape.type(), converted.type());
			assertEquals(shape.name(), converted.name());
			assertEquals(shape.tags(), converted.tags());
		}

		@Test
		void fromSchematicData_shouldPreserveDeprecatedTarget() {
			com.sigmundgranaas.forgero.data.loading.api.data.SchematicData schematic =
					new com.sigmundgranaas.forgero.data.loading.api.data.SchematicData(
							id("forgero:schematic"),
							"refined_pickaxe",
							null, null, null, null, null, null, null,
							id("forgero:parts/pickaxe_head"),
							null
					);

			ResourceData converted = ResourceData.from(schematic);

			assertEquals(schematic.target(), converted.target());
		}

		@Test
		void fromStaticData_shouldPreserveUpgrades() {
			com.sigmundgranaas.forgero.data.loading.api.data.StaticData staticData =
					new com.sigmundgranaas.forgero.data.loading.api.data.StaticData(
							id("forgero:static_part"),
							"oak_handle",
							null, null, null, null,
							List.of(), // upgrades
							null
					);

			ResourceData converted = ResourceData.from(staticData);

			assertNotNull(converted.upgrades());
		}
	}
}
