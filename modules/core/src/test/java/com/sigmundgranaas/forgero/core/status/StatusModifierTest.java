package com.sigmundgranaas.forgero.core.status;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifier Tests")
class StatusModifierTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	@Nested
	@DisplayName("SimpleStatusModifier Construction")
	class Construction {

		@Test
		@DisplayName("should create modifier with all fields")
		void shouldCreateModifierWithAllFields() {
			StatusModifier modifier = new SimpleStatusModifier(
					id("sharp"),
					"Sharp",
					5,
					Set.of(id("blunt")),
					Map.of("forgero:attributes", List.of("attr1"))
			);

			assertEquals(id("sharp"), modifier.id());
			assertEquals("Sharp", modifier.displayName());
			assertEquals(5, modifier.priority());
			assertTrue(modifier.incompatibilities().contains(id("blunt")));
			assertFalse(modifier.propertiesAsMap().isEmpty());
		}

		@Test
		@DisplayName("should create modifier with minimal fields")
		void shouldCreateModifierWithMinimalFields() {
			StatusModifier modifier = SimpleStatusModifier.of(id("basic"), "Basic");

			assertEquals(id("basic"), modifier.id());
			assertEquals("Basic", modifier.displayName());
			assertEquals(0, modifier.priority());
			assertTrue(modifier.incompatibilities().isEmpty());
			assertTrue(modifier.propertiesAsMap().isEmpty());
		}

		@Test
		@DisplayName("should create modifier using builder")
		void shouldCreateModifierUsingBuilder() {
			StatusModifier modifier = SimpleStatusModifier.builder("forgero:durable")
					.displayName("Durable")
					.priority(3)
					.incompatibleWith("forgero:fragile", "forgero:weak")
					.property("forgero:attributes", List.of("durability"))
					.build();

			assertEquals(OpenIdentifier.parse("forgero:durable"), modifier.id());
			assertEquals("Durable", modifier.displayName());
			assertEquals(3, modifier.priority());
			assertEquals(2, modifier.incompatibilities().size());
			assertTrue(modifier.incompatibilities().contains(OpenIdentifier.parse("forgero:fragile")));
		}

		@Test
		@DisplayName("should reject null id")
		void shouldRejectNullId() {
			assertThrows(NullPointerException.class, () ->
					new SimpleStatusModifier(null, "Name", 0, Set.of(), Map.of())
			);
		}

		@Test
		@DisplayName("should reject null displayName")
		void shouldRejectNullDisplayName() {
			assertThrows(NullPointerException.class, () ->
					new SimpleStatusModifier(id("test"), null, 0, Set.of(), Map.of())
			);
		}

		@Test
		@DisplayName("should handle null incompatibilities as empty set")
		void shouldHandleNullIncompatibilitiesAsEmptySet() {
			StatusModifier modifier = new SimpleStatusModifier(
					id("test"),
					"Test",
					0,
					null,
					null
			);

			assertNotNull(modifier.incompatibilities());
			assertTrue(modifier.incompatibilities().isEmpty());
		}

		@Test
		@DisplayName("should create defensive copy of incompatibilities")
		void shouldCreateDefensiveCopyOfIncompatibilities() {
			Set<OpenIdentifier> original = new java.util.HashSet<>();
			original.add(id("blunt"));

			StatusModifier modifier = new SimpleStatusModifier(
					id("sharp"),
					"Sharp",
					0,
					original,
					Map.of()
			);

			original.add(id("dull")); // Modify original

			// Modifier's set should be unchanged
			assertEquals(1, modifier.incompatibilities().size());
			assertFalse(modifier.incompatibilities().contains(id("dull")));
		}
	}

	@Nested
	@DisplayName("Incompatibility Checking")
	class IncompatibilityChecking {

		@Test
		@DisplayName("should detect direct incompatibility")
		void shouldDetectDirectIncompatibility() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.incompatibleWith("forgero:blunt")
					.build();

			StatusModifier blunt = SimpleStatusModifier.builder("forgero:blunt")
					.build();

			assertTrue(sharp.isIncompatibleWith(blunt));
		}

		@Test
		@DisplayName("should detect reverse incompatibility")
		void shouldDetectReverseIncompatibility() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.build();

			StatusModifier blunt = SimpleStatusModifier.builder("forgero:blunt")
					.incompatibleWith("forgero:sharp")
					.build();

			assertTrue(sharp.isIncompatibleWith(blunt));
		}

		@Test
		@DisplayName("should detect mutual incompatibility")
		void shouldDetectMutualIncompatibility() {
			StatusModifier mod1 = SimpleStatusModifier.builder("forgero:fire")
					.incompatibleWith("forgero:ice")
					.build();

			StatusModifier mod2 = SimpleStatusModifier.builder("forgero:ice")
					.incompatibleWith("forgero:fire")
					.build();

			assertTrue(mod1.isIncompatibleWith(mod2));
			assertTrue(mod2.isIncompatibleWith(mod1));
		}

		@Test
		@DisplayName("should return false for compatible modifiers")
		void shouldReturnFalseForCompatibleModifiers() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.incompatibleWith("forgero:blunt")
					.build();

			StatusModifier durable = SimpleStatusModifier.builder("forgero:durable")
					.build();

			assertFalse(sharp.isIncompatibleWith(durable));
			assertFalse(durable.isIncompatibleWith(sharp));
		}

		@Test
		@DisplayName("should check incompatibility with any in collection")
		void shouldCheckIncompatibilityWithAnyInCollection() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.incompatibleWith("forgero:blunt")
					.build();

			List<StatusModifier> modifiers = List.of(
					SimpleStatusModifier.builder("forgero:durable").build(),
					SimpleStatusModifier.builder("forgero:blunt").build(),
					SimpleStatusModifier.builder("forgero:shiny").build()
			);

			assertTrue(sharp.isIncompatibleWithAny(modifiers));
		}

		@Test
		@DisplayName("should return false when compatible with all in collection")
		void shouldReturnFalseWhenCompatibleWithAllInCollection() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.incompatibleWith("forgero:blunt")
					.build();

			List<StatusModifier> modifiers = List.of(
					SimpleStatusModifier.builder("forgero:durable").build(),
					SimpleStatusModifier.builder("forgero:shiny").build()
			);

			assertFalse(sharp.isIncompatibleWithAny(modifiers));
		}

		@Test
		@DisplayName("should handle empty collection")
		void shouldHandleEmptyCollection() {
			StatusModifier sharp = SimpleStatusModifier.builder("forgero:sharp")
					.incompatibleWith("forgero:blunt")
					.build();

			assertFalse(sharp.isIncompatibleWithAny(List.of()));
		}
	}

	@Nested
	@DisplayName("PropertyHolder Implementation")
	class PropertyHolderImplementation {

		@Test
		@DisplayName("should return properties as map")
		void shouldReturnPropertiesAsMap() {
			StatusModifier modifier = new SimpleStatusModifier(
					id("test"),
					"Test",
					0,
					Set.of(),
					Map.of(
							"forgero:attributes", List.of("durability", "attack_damage"),
							"forgero:features", List.of("feature1")
					)
			);

			Map<String, List<?>> props = modifier.propertiesAsMap();
			assertEquals(2, props.size());
			assertEquals(2, props.get("forgero:attributes").size());
			assertEquals(1, props.get("forgero:features").size());
		}

		@Test
		@DisplayName("should return empty map when no properties")
		void shouldReturnEmptyMapWhenNoProperties() {
			StatusModifier modifier = SimpleStatusModifier.of(id("empty"), "Empty");

			assertTrue(modifier.propertiesAsMap().isEmpty());
		}

		@Test
		@DisplayName("properties map should be immutable")
		void propertiesMapShouldBeImmutable() {
			StatusModifier modifier = new SimpleStatusModifier(
					id("test"),
					"Test",
					0,
					Set.of(),
					Map.of("key", List.of("value"))
			);

			Map<String, List<?>> props = modifier.propertiesAsMap();
			assertThrows(UnsupportedOperationException.class, () ->
					props.put("newKey", List.of())
			);
		}
	}
}
