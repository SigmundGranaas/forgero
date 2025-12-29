package com.sigmundgranaas.forgero.core.status;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierDefinition;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierRegistry;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlot;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlotContainer;
import com.sigmundgranaas.forgero.core.status.impl.StatusModifierConverter;
import com.sigmundgranaas.forgero.data.loading.api.data.StatusModifierData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.StatusModifierDataCodec;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Status Modifier Integration Tests")
class StatusModifierIntegrationTest {

	@BeforeEach
	void setUp() {
		StatusModifierRegistry.clear();
	}

	@AfterEach
	void tearDown() {
		StatusModifierRegistry.clear();
	}

	@Nested
	@DisplayName("Full Data Loading Pipeline")
	class FullDataLoadingPipeline {

		private static final String SHARP_JSON = """
				{
				  "id": "forgero:sharp",
				  "display_name": "Sharp",
				  "priority": 5,
				  "target": {
				    "types": ["forgero:tool", "forgero:weapon"],
				    "incompatibilities": ["forgero:blunt"]
				  },
				  "chance": 0.04,
				  "attributes": [
				    { "type": "forgero:attack_damage", "computation": 3 }
				  ]
				}
				""";

		private static final String DURABLE_JSON = """
				{
				  "id": "forgero:durable",
				  "display_name": "Durable",
				  "priority": 3,
				  "target": {
				    "types": ["forgero:tool", "forgero:armor"],
				    "incompatibilities": ["forgero:fragile"]
				  },
				  "chance": 0.05,
				  "attributes": [
				    { "type": "forgero:durability", "computation": 50 }
				  ]
				}
				""";

		private static final String BLUNT_JSON = """
				{
				  "id": "forgero:blunt",
				  "display_name": "Blunt",
				  "priority": 2,
				  "target": {
				    "types": ["forgero:weapon"],
				    "incompatibilities": ["forgero:sharp"]
				  },
				  "chance": 0.02
				}
				""";

		@Test
		@DisplayName("should load, convert, and register modifiers")
		void shouldLoadConvertAndRegisterModifiers() {
			var codec = StatusModifierDataCodec.createSimple();

			// Step 1: Parse JSON to Data
			StatusModifierData sharpData = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(SHARP_JSON))
					.result().orElseThrow();
			StatusModifierData durableData = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(DURABLE_JSON))
					.result().orElseThrow();

			// Step 2: Convert to runtime types
			StatusModifierDefinition sharpDef = StatusModifierConverter.toDefinition(sharpData);
			StatusModifierDefinition durableDef = StatusModifierConverter.toDefinition(durableData);

			// Step 3: Register
			StatusModifierRegistry.register(sharpDef);
			StatusModifierRegistry.register(durableDef);

			// Verify
			assertEquals(2, StatusModifierRegistry.size());
			assertTrue(StatusModifierRegistry.isRegistered(OpenIdentifier.of("sharp")));
			assertTrue(StatusModifierRegistry.isRegistered(OpenIdentifier.of("durable")));
		}

		@Test
		@DisplayName("should query registered modifiers by type")
		void shouldQueryRegisteredModifiersByType() {
			var codec = StatusModifierDataCodec.createSimple();

			// Load and register all modifiers
			List.of(SHARP_JSON, DURABLE_JSON, BLUNT_JSON).forEach(json -> {
				StatusModifierData data = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
						.result().orElseThrow();
				StatusModifierRegistry.register(StatusModifierConverter.toDefinition(data));
			});

			// Query for tool modifiers
			List<StatusModifierDefinition> toolModifiers =
					StatusModifierRegistry.findApplicable(Set.of(OpenIdentifier.of("tool")));

			// Sharp and Durable apply to tools
			assertEquals(2, toolModifiers.size());
		}

		@Test
		@DisplayName("should preserve incompatibilities through conversion")
		void shouldPreserveIncompatibilitiesThroughConversion() {
			var codec = StatusModifierDataCodec.createSimple();

			StatusModifierData sharpData = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(SHARP_JSON))
					.result().orElseThrow();
			StatusModifierData bluntData = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(BLUNT_JSON))
					.result().orElseThrow();

			StatusModifier sharp = StatusModifierConverter.toModifier(sharpData);
			StatusModifier blunt = StatusModifierConverter.toModifier(bluntData);

			// Sharp should be incompatible with blunt
			assertTrue(sharp.isIncompatibleWith(blunt));
			// Blunt should be incompatible with sharp (mutual)
			assertTrue(blunt.isIncompatibleWith(sharp));
		}
	}

	@Nested
	@DisplayName("Component Integration Scenario")
	class ComponentIntegrationScenario {

		@Test
		@DisplayName("should install modifiers into slot container")
		void shouldInstallModifiersIntoSlotContainer() {
			var codec = StatusModifierDataCodec.createSimple();

			// Load modifiers
			String json = """
					{
					  "id": "forgero:enchanted",
					  "display_name": "Enchanted",
					  "priority": 10
					}
					""";

			StatusModifierData data = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
					.result().orElseThrow();
			StatusModifier modifier = StatusModifierConverter.toModifier(data);

			// Create a component with status modifier slots
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(3);

			// Install modifier
			Optional<StatusModifierSlotContainer> result = container.tryInstall(modifier);

			assertTrue(result.isPresent());
			StatusModifierSlotContainer updated = result.get();

			assertEquals(1, updated.filledCount());
			assertTrue(updated.hasModifier(OpenIdentifier.of("enchanted")));
		}

		@Test
		@DisplayName("should prevent incompatible modifier installation")
		void shouldPreventIncompatibleModifierInstallation() {
			var codec = StatusModifierDataCodec.createSimple();

			String fireJson = """
					{
					  "id": "forgero:fire",
					  "display_name": "Fire",
					  "target": { "incompatibilities": ["forgero:ice"] }
					}
					""";

			String iceJson = """
					{
					  "id": "forgero:ice",
					  "display_name": "Ice",
					  "target": { "incompatibilities": ["forgero:fire"] }
					}
					""";

			StatusModifier fire = StatusModifierConverter.toModifier(
					codec.parse(JsonOps.INSTANCE, JsonParser.parseString(fireJson)).result().orElseThrow()
			);
			StatusModifier ice = StatusModifierConverter.toModifier(
					codec.parse(JsonOps.INSTANCE, JsonParser.parseString(iceJson)).result().orElseThrow()
			);

			// Install fire first
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(2);
			container = container.tryInstall(fire).orElseThrow();

			// Try to install ice - should fail
			Optional<StatusModifierSlotContainer> result = container.tryInstall(ice);

			assertTrue(result.isEmpty());
			assertFalse(container.hasModifier(OpenIdentifier.of("ice")));
		}

		@Test
		@DisplayName("should order modifiers by priority")
		void shouldOrderModifiersByPriority() {
			var codec = StatusModifierDataCodec.createSimple();

			// Create modifiers with different priorities
			String[] jsons = {
					"""
					{ "id": "forgero:low", "priority": 1 }
					""",
					"""
					{ "id": "forgero:high", "priority": 10 }
					""",
					"""
					{ "id": "forgero:medium", "priority": 5 }
					"""
			};

			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(3);

			for (String json : jsons) {
				StatusModifier modifier = StatusModifierConverter.toModifier(
						codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow()
				);
				container = container.tryInstall(modifier).orElseThrow();
			}

			List<StatusModifier> ordered = container.modifiersByPriority();

			assertEquals(3, ordered.size());
			assertEquals(OpenIdentifier.of("high"), ordered.get(0).id());
			assertEquals(OpenIdentifier.of("medium"), ordered.get(1).id());
			assertEquals(OpenIdentifier.of("low"), ordered.get(2).id());
		}
	}

	@Nested
	@DisplayName("Registry Lookup Scenario")
	class RegistryLookupScenario {

		@Test
		@DisplayName("should look up and install modifier by id")
		void shouldLookUpAndInstallModifierById() {
			var codec = StatusModifierDataCodec.createSimple();

			// Register modifiers
			String json = """
					{
					  "id": "forgero:golden",
					  "display_name": "Golden",
					  "priority": 8
					}
					""";

			StatusModifierData data = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
					.result().orElseThrow();
			StatusModifierRegistry.register(StatusModifierConverter.toDefinition(data));

			// Look up and install
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(1);

			Optional<StatusModifier> modifier = StatusModifierRegistry.getModifier(OpenIdentifier.of("golden"));
			assertTrue(modifier.isPresent());

			container = container.withModifier(modifier.get());

			assertTrue(container.hasModifier(OpenIdentifier.of("golden")));
		}

		@Test
		@DisplayName("should find random applicable modifiers for component")
		void shouldFindRandomApplicableModifiersForComponent() {
			var codec = StatusModifierDataCodec.createSimple();

			// Register several modifiers with different chances
			String[] jsons = {
					"""
					{ "id": "forgero:common", "chance": 0.5, "target": { "types": ["forgero:sword"] } }
					""",
					"""
					{ "id": "forgero:rare", "chance": 0.01, "target": { "types": ["forgero:sword"] } }
					""",
					"""
					{ "id": "forgero:guaranteed", "chance": 0.0, "target": { "types": ["forgero:sword"] } }
					""",
					"""
					{ "id": "forgero:axe_only", "chance": 0.5, "target": { "types": ["forgero:axe"] } }
					"""
			};

			for (String json : jsons) {
				StatusModifierData data = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
						.result().orElseThrow();
				StatusModifierRegistry.register(StatusModifierConverter.toDefinition(data));
			}

			// Find modifiers that can randomly occur on a sword
			Set<OpenIdentifier> swordTags = Set.of(OpenIdentifier.of("sword"));
			List<StatusModifierDefinition> applicable = StatusModifierRegistry.findRandomApplicable(swordTags);

			// Should find common and rare (non-zero chance), not guaranteed (zero) or axe_only (wrong type)
			assertEquals(2, applicable.size());
		}
	}

	@Nested
	@DisplayName("Slot Serialization Roundtrip")
	class SlotSerializationRoundtrip {

		@Test
		@DisplayName("should serialize and deserialize slot")
		void shouldSerializeAndDeserializeSlot() {
			// Create a slot
			StatusModifierSlot original = StatusModifierSlot.empty(OpenIdentifier.of("test_slot"), 5);

			// Serialize
			var encoded = StatusModifierSlot.CODEC.encodeStart(JsonOps.INSTANCE, original);
			assertTrue(encoded.result().isPresent());

			// Deserialize
			var decoded = StatusModifierSlot.CODEC.parse(JsonOps.INSTANCE, encoded.result().get());
			assertTrue(decoded.result().isPresent());

			StatusModifierSlot restored = decoded.result().get();

			assertEquals(original.id(), restored.id());
			assertEquals(original.index(), restored.index());
		}
	}
}
