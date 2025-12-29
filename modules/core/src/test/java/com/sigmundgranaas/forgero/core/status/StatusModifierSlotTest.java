package com.sigmundgranaas.forgero.core.status;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlot;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifierSlot Tests")
class StatusModifierSlotTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	private static StatusModifier createModifier(String name) {
		return SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1))
				.build();
	}

	@Nested
	@DisplayName("Slot Construction")
	class Construction {

		@Test
		@DisplayName("should create empty slot")
		void shouldCreateEmptySlot() {
			StatusModifierSlot slot = StatusModifierSlot.empty(id("slot_0"), 0);

			assertEquals(id("slot_0"), slot.id());
			assertEquals(0, slot.index());
			assertTrue(slot.isEmpty());
			assertFalse(slot.isFilled());
			assertTrue(slot.content().isEmpty());
		}

		@Test
		@DisplayName("should create filled slot")
		void shouldCreateFilledSlot() {
			StatusModifier modifier = createModifier("sharp");
			StatusModifierSlot slot = StatusModifierSlot.filled(id("slot_0"), 0, modifier);

			assertFalse(slot.isEmpty());
			assertTrue(slot.isFilled());
			assertTrue(slot.content().isPresent());
			assertEquals(modifier, slot.content().get());
		}

		@Test
		@DisplayName("should create slot with emptyAt factory")
		void shouldCreateSlotWithEmptyAtFactory() {
			StatusModifierSlot slot = StatusModifierSlot.emptyAt(5);

			assertEquals(id("status_slot_5"), slot.id());
			assertEquals(5, slot.index());
			assertTrue(slot.isEmpty());
		}

		@Test
		@DisplayName("should have correct type constant")
		void shouldHaveCorrectTypeConstant() {
			assertEquals("forgero:status_modifier", StatusModifierSlot.TYPE);
		}

		@Test
		@DisplayName("should have correct slot type")
		void shouldHaveCorrectSlotType() {
			StatusModifierSlot slot = StatusModifierSlot.emptyAt(0);
			assertEquals(id("status_modifier"), slot.slotType());
		}
	}

	@Nested
	@DisplayName("Slot Interface Implementation")
	class SlotInterface {

		@Test
		@DisplayName("should return correct type identifier")
		void shouldReturnCorrectTypeIdentifier() {
			StatusModifierSlot slot = StatusModifierSlot.emptyAt(0);
			assertEquals(OpenIdentifier.parse("forgero:status_modifier"), slot.type());
		}

		@Test
		@DisplayName("should include in traversal when filled")
		void shouldIncludeInTraversalWhenFilled() {
			StatusModifier modifier = createModifier("sharp");
			StatusModifierSlot slot = StatusModifierSlot.filled(id("slot_0"), 0, modifier);

			assertTrue(slot.includeInTraversal());
		}

		@Test
		@DisplayName("should not include in traversal when empty")
		void shouldNotIncludeInTraversalWhenEmpty() {
			StatusModifierSlot slot = StatusModifierSlot.emptyAt(0);

			assertFalse(slot.includeInTraversal());
		}

		@Test
		@DisplayName("should generate description for empty slot")
		void shouldGenerateDescriptionForEmptySlot() {
			StatusModifierSlot slot = StatusModifierSlot.emptyAt(0);

			assertEquals("Empty status modifier slot", slot.description());
		}

		@Test
		@DisplayName("should generate description for filled slot")
		void shouldGenerateDescriptionForFilledSlot() {
			StatusModifier modifier = createModifier("sharp");
			StatusModifierSlot slot = StatusModifierSlot.filled(id("slot_0"), 0, modifier);

			assertTrue(slot.description().contains("Sharp"));
			assertTrue(slot.description().startsWith("Status:"));
		}
	}

	@Nested
	@DisplayName("Slot Transformations")
	class Transformations {

		@Test
		@DisplayName("should create new slot when adding modifier")
		void shouldCreateNewSlotWhenAddingModifier() {
			StatusModifierSlot original = StatusModifierSlot.emptyAt(0);
			StatusModifier modifier = createModifier("sharp");

			StatusModifierSlot filled = original.withModifier(modifier);

			// Original unchanged
			assertTrue(original.isEmpty());

			// New slot is filled
			assertTrue(filled.isFilled());
			assertEquals(modifier, filled.content().get());
			assertEquals(original.id(), filled.id());
			assertEquals(original.index(), filled.index());
		}

		@Test
		@DisplayName("should create empty slot from filled")
		void shouldCreateEmptySlotFromFilled() {
			StatusModifier modifier = createModifier("sharp");
			StatusModifierSlot filled = StatusModifierSlot.filled(id("slot_0"), 0, modifier);

			StatusModifierSlot emptied = filled.empty();

			// Original unchanged
			assertTrue(filled.isFilled());

			// New slot is empty
			assertTrue(emptied.isEmpty());
			assertEquals(filled.id(), emptied.id());
			assertEquals(filled.index(), emptied.index());
		}

		@Test
		@DisplayName("should preserve slot configuration when transforming")
		void shouldPreserveSlotConfigurationWhenTransforming() {
			StatusModifierSlot slot = new StatusModifierSlot(
					id("custom_slot"),
					42,
					Optional.empty()
			);

			StatusModifier modifier = createModifier("durable");
			StatusModifierSlot transformed = slot.withModifier(modifier);

			assertEquals(id("custom_slot"), transformed.id());
			assertEquals(42, transformed.index());
		}

		@Test
		@DisplayName("should replace modifier when already filled")
		void shouldReplaceModifierWhenAlreadyFilled() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			StatusModifierSlot slot = StatusModifierSlot.filled(id("slot_0"), 0, sharp);
			StatusModifierSlot replaced = slot.withModifier(durable);

			assertEquals(durable, replaced.content().get());
		}
	}

	@Nested
	@DisplayName("Codec Tests")
	class CodecTests {

		@Test
		@DisplayName("should serialize empty slot to JSON")
		void shouldSerializeEmptySlotToJson() {
			StatusModifierSlot slot = StatusModifierSlot.empty(id("slot_0"), 0);

			var result = StatusModifierSlot.CODEC.encodeStart(JsonOps.INSTANCE, slot);
			assertTrue(result.result().isPresent());

			String json = result.result().get().toString();
			assertTrue(json.contains("slot_0"));
		}

		@Test
		@DisplayName("should deserialize slot from JSON")
		void shouldDeserializeSlotFromJson() {
			String json = """
					{
					  "id": "forgero:test_slot",
					  "index": 3
					}
					""";

			DataResult<StatusModifierSlot> result = StatusModifierSlot.CODEC.parse(
					JsonOps.INSTANCE,
					JsonParser.parseString(json)
			);

			assertTrue(result.result().isPresent());
			StatusModifierSlot slot = result.result().get();

			assertEquals(OpenIdentifier.parse("forgero:test_slot"), slot.id());
			assertEquals(3, slot.index());
			assertTrue(slot.isEmpty()); // Codec creates empty slots
		}

		@Test
		@DisplayName("should use default index when not specified")
		void shouldUseDefaultIndexWhenNotSpecified() {
			String json = """
					{
					  "id": "forgero:simple_slot"
					}
					""";

			DataResult<StatusModifierSlot> result = StatusModifierSlot.CODEC.parse(
					JsonOps.INSTANCE,
					JsonParser.parseString(json)
			);

			assertTrue(result.result().isPresent());
			assertEquals(0, result.result().get().index());
		}

		@Test
		@DisplayName("should fail when id is missing")
		void shouldFailWhenIdIsMissing() {
			String json = """
					{
					  "index": 0
					}
					""";

			DataResult<StatusModifierSlot> result = StatusModifierSlot.CODEC.parse(
					JsonOps.INSTANCE,
					JsonParser.parseString(json)
			);

			assertTrue(result.error().isPresent());
		}
	}

	@Nested
	@DisplayName("Record Equality")
	class RecordEquality {

		@Test
		@DisplayName("equal slots should be equal")
		void equalSlotsShouldBeEqual() {
			StatusModifierSlot slot1 = StatusModifierSlot.empty(id("slot"), 0);
			StatusModifierSlot slot2 = StatusModifierSlot.empty(id("slot"), 0);

			assertEquals(slot1, slot2);
			assertEquals(slot1.hashCode(), slot2.hashCode());
		}

		@Test
		@DisplayName("different slots should not be equal")
		void differentSlotsShouldNotBeEqual() {
			StatusModifierSlot slot1 = StatusModifierSlot.empty(id("slot_1"), 0);
			StatusModifierSlot slot2 = StatusModifierSlot.empty(id("slot_2"), 0);

			assertNotEquals(slot1, slot2);
		}

		@Test
		@DisplayName("filled and empty slots should not be equal")
		void filledAndEmptySlotsShouldNotBeEqual() {
			StatusModifier modifier = createModifier("sharp");
			StatusModifierSlot empty = StatusModifierSlot.empty(id("slot"), 0);
			StatusModifierSlot filled = StatusModifierSlot.filled(id("slot"), 0, modifier);

			assertNotEquals(empty, filled);
		}
	}
}
