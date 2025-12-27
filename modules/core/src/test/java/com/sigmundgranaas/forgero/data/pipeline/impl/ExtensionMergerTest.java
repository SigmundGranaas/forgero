package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionMergerTest {

	private ExtensionMerger merger;
	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		merger = new ExtensionMerger();
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	@Nested
	class WhenNoExtensionsPresent {

		@Test
		void shouldReturnOriginalDefinitionsUnchanged() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null,
					null,
					null,
					null,
					null
			);

			Map<OpenIdentifier, RawDefinition> definitions = Map.of(
					id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron)
			);

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertEquals(1, result.size());
			assertSame(iron, result.get(id("forgero:materials/iron")).data());
		}
	}

	@Nested
	class WhenMergingTags {

		@Test
		void shouldUnionTagsFromExtension() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material"), id("forgero:armor_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-tools"), new RawDefinition(id("forgero:extensions/iron-tools"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertEquals(1, result.size());
			assertFalse(result.containsKey(id("forgero:extensions/iron-tools")));

			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();
			assertNotNull(merged.tags());
			assertEquals(3, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:metal")));
			assertTrue(merged.tags().contains(id("forgero:tool_material")));
			assertTrue(merged.tags().contains(id("forgero:armor_material")));
		}

		@Test
		void shouldNotDuplicateTags() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal"), id("forgero:tool_material")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material"), id("forgero:armor_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			// Should have 3 unique tags, not 4
			assertEquals(3, merged.tags().size());
		}
	}

	@Nested
	class WhenMergingAttributes {

		@Test
		void shouldConcatenateAttributes() {
			AttributeData durability = new AttributeDataImpl(
					id("forgero:iron-durability"),
					id("forgero:durability"),
					new ComputationData(250f, null, null),
					null
			);

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null,
					List.of(durability),
					null, null
			);

			AttributeData miningSpeed = new AttributeDataImpl(
					id("forgero:iron-mining-speed"),
					id("forgero:mining_speed"),
					new ComputationData(6f, null, null),
					null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null,
					List.of(miningSpeed),
					null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-tools"), new RawDefinition(id("forgero:extensions/iron-tools"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.attributes());
			assertEquals(2, merged.attributes().size());
			assertTrue(merged.attributes().stream().anyMatch(a -> a.id().equals(id("forgero:iron-durability"))));
			assertTrue(merged.attributes().stream().anyMatch(a -> a.id().equals(id("forgero:iron-mining-speed"))));
		}
	}

	@Nested
	class WhenMergingProperties {

		@Test
		void shouldMergeDistinctPropertyKeys() {
			JsonArray tooltips = new JsonArray();
			tooltips.add(new JsonPrimitive("Base tooltip"));
			Map<String, com.google.gson.JsonElement> baseProps = Map.of("forgero:tooltip", tooltips);

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					baseProps
			);

			JsonArray features = new JsonArray();
			features.add(new JsonPrimitive("vein_mining"));
			Map<String, com.google.gson.JsonElement> extProps = Map.of("forgero:features", features);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					extProps
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.properties());
			assertEquals(2, merged.properties().size());
			assertTrue(merged.properties().containsKey("forgero:tooltip"));
			assertTrue(merged.properties().containsKey("forgero:features"));
		}

		@Test
		void shouldConcatenateArrayProperties() {
			JsonArray baseTooltips = new JsonArray();
			baseTooltips.add(new JsonPrimitive("Base tooltip"));

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					Map.of("forgero:tooltip", baseTooltips)
			);

			JsonArray extTooltips = new JsonArray();
			extTooltips.add(new JsonPrimitive("Extension tooltip"));

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					Map.of("forgero:tooltip", extTooltips)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			JsonArray mergedTooltips = merged.properties().get("forgero:tooltip").getAsJsonArray();
			assertEquals(2, mergedTooltips.size());
		}

		@Test
		void shouldDeepMergeObjectProperties() {
			JsonObject baseConfig = new JsonObject();
			baseConfig.addProperty("debug", false);
			baseConfig.addProperty("version", "1.0");

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					Map.of("config", baseConfig)
			);

			JsonObject extConfig = new JsonObject();
			extConfig.addProperty("debug", true);
			extConfig.addProperty("extra", "value");

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					Map.of("config", extConfig)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			JsonObject mergedConfig = merged.properties().get("config").getAsJsonObject();
			// Extension value overwrites base for primitives
			assertTrue(mergedConfig.get("debug").getAsBoolean());
			// Base value preserved
			assertEquals("1.0", mergedConfig.get("version").getAsString());
			// Extension value added
			assertEquals("value", mergedConfig.get("extra").getAsString());
		}
	}

	@Nested
	class WhenMultipleExtensions {

		@Test
		void shouldApplyInPriorityOrder() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:base")),
					null, null, null, null, null
			);

			ExtensionData lowPriority = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:first")),
					null, null
			);

			ExtensionData highPriority = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					100,
					List.of(id("forgero:second")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-low"), new RawDefinition(id("forgero:extensions/iron-low"), lowPriority));
			definitions.put(id("forgero:extensions/iron-high"), new RawDefinition(id("forgero:extensions/iron-high"), highPriority));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			// All tags should be present in order
			assertEquals(3, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:base")));
			assertTrue(merged.tags().contains(id("forgero:first")));
			assertTrue(merged.tags().contains(id("forgero:second")));
		}
	}

	@Nested
	class WhenTargetNotFound {

		@Test
		void shouldIgnoreOrphanedExtensions() {
			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/nonexistent"),
					0,
					List.of(id("forgero:tag")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:extensions/orphan"), new RawDefinition(id("forgero:extensions/orphan"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertTrue(result.isEmpty());
		}
	}

	@Nested
	class WhenMergingDifferentTypes {

		@Test
		void shouldMergeIntoShapeData() {
			ResourceData shape = new ResourceData(
					id("forgero:shape"),
					"pickaxe_head",
					null,
					List.of(id("forgero:tool_shape")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:shapes/pickaxe_head"),
					0,
					List.of(id("forgero:mining_shape")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:shapes/pickaxe_head"), new RawDefinition(id("forgero:shapes/pickaxe_head"), shape));
			definitions.put(id("forgero:extensions/pickaxe-ext"), new RawDefinition(id("forgero:extensions/pickaxe-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:shapes/pickaxe_head")).data();

			assertEquals(2, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:tool_shape")));
			assertTrue(merged.tags().contains(id("forgero:mining_shape")));
		}
	}

	@Nested
	class WhenExtensionAddsToNullFields {

		@Test
		void shouldCreateFieldsWhenBaseIsNull() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.tags());
			assertEquals(1, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:tool_material")));
		}
	}
}
