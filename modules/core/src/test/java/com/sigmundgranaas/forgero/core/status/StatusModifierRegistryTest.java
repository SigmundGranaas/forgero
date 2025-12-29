package com.sigmundgranaas.forgero.core.status;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierDefinition;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierRegistry;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifier;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifierDefinition;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifierRegistry Tests")
class StatusModifierRegistryTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	private static StatusModifier createModifier(String name) {
		return SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1))
				.build();
	}

	private static StatusModifierDefinition createDefinition(String name, Set<OpenIdentifier> targetTypes, float chance) {
		return new SimpleStatusModifierDefinition(
				createModifier(name),
				targetTypes,
				Set.of(),
				chance
		);
	}

	@BeforeEach
	void setUp() {
		// Clear registry before each test
		StatusModifierRegistry.clear();
	}

	@AfterEach
	void tearDown() {
		// Clean up after each test
		StatusModifierRegistry.clear();
	}

	@Nested
	@DisplayName("Registration")
	class Registration {

		@Test
		@DisplayName("should register definition successfully")
		void shouldRegisterDefinitionSuccessfully() {
			StatusModifierDefinition definition = createDefinition("sharp", Set.of(id("tool")), 0.1f);

			assertDoesNotThrow(() -> StatusModifierRegistry.register(definition));
			assertTrue(StatusModifierRegistry.isRegistered(id("sharp")));
		}

		@Test
		@DisplayName("should reject duplicate registration")
		void shouldRejectDuplicateRegistration() {
			StatusModifierDefinition definition = createDefinition("sharp", Set.of(), 0f);
			StatusModifierRegistry.register(definition);

			assertThrows(IllegalArgumentException.class, () ->
					StatusModifierRegistry.register(definition)
			);
		}

		@Test
		@DisplayName("should allow registerOrReplace for duplicates")
		void shouldAllowRegisterOrReplaceForDuplicates() {
			StatusModifierDefinition original = createDefinition("sharp", Set.of(id("tool")), 0.1f);
			StatusModifierDefinition replacement = createDefinition("sharp", Set.of(id("weapon")), 0.2f);

			StatusModifierRegistry.register(original);
			assertDoesNotThrow(() -> StatusModifierRegistry.registerOrReplace(replacement));

			// Should have the replacement
			StatusModifierDefinition found = StatusModifierRegistry.get(id("sharp")).orElseThrow();
			assertTrue(found.targetTypes().contains(id("weapon")));
			assertEquals(0.2f, found.chance());
		}
	}

	@Nested
	@DisplayName("Query Operations")
	class QueryOperations {

		@Test
		@DisplayName("should get definition by id")
		void shouldGetDefinitionById() {
			StatusModifierDefinition definition = createDefinition("sharp", Set.of(), 0f);
			StatusModifierRegistry.register(definition);

			Optional<StatusModifierDefinition> found = StatusModifierRegistry.get(id("sharp"));

			assertTrue(found.isPresent());
			assertEquals(definition, found.get());
		}

		@Test
		@DisplayName("should return empty for unknown id")
		void shouldReturnEmptyForUnknownId() {
			Optional<StatusModifierDefinition> found = StatusModifierRegistry.get(id("unknown"));

			assertTrue(found.isEmpty());
		}

		@Test
		@DisplayName("should get modifier by id")
		void shouldGetModifierById() {
			StatusModifierDefinition definition = createDefinition("sharp", Set.of(), 0f);
			StatusModifierRegistry.register(definition);

			Optional<StatusModifier> found = StatusModifierRegistry.getModifier(id("sharp"));

			assertTrue(found.isPresent());
			assertEquals("Sharp", found.get().displayName());
		}

		@Test
		@DisplayName("should get modifier by string id")
		void shouldGetModifierByStringId() {
			StatusModifierDefinition definition = createDefinition("sharp", Set.of(), 0f);
			StatusModifierRegistry.register(definition);

			Optional<StatusModifier> found = StatusModifierRegistry.getModifier("forgero:sharp");

			assertTrue(found.isPresent());
		}

		@Test
		@DisplayName("should return all definitions")
		void shouldReturnAllDefinitions() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));
			StatusModifierRegistry.register(createDefinition("durable", Set.of(), 0f));
			StatusModifierRegistry.register(createDefinition("lucky", Set.of(), 0f));

			List<StatusModifierDefinition> all = StatusModifierRegistry.all().toList();

			assertEquals(3, all.size());
		}

		@Test
		@DisplayName("should return all modifiers")
		void shouldReturnAllModifiers() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));
			StatusModifierRegistry.register(createDefinition("durable", Set.of(), 0f));

			List<StatusModifier> modifiers = StatusModifierRegistry.allModifiers().toList();

			assertEquals(2, modifiers.size());
		}

		@Test
		@DisplayName("should return all ids")
		void shouldReturnAllIds() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));
			StatusModifierRegistry.register(createDefinition("durable", Set.of(), 0f));

			Set<OpenIdentifier> ids = StatusModifierRegistry.allIds();

			assertEquals(2, ids.size());
			assertTrue(ids.contains(id("sharp")));
			assertTrue(ids.contains(id("durable")));
		}

		@Test
		@DisplayName("should return correct size")
		void shouldReturnCorrectSize() {
			assertEquals(0, StatusModifierRegistry.size());

			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));
			assertEquals(1, StatusModifierRegistry.size());

			StatusModifierRegistry.register(createDefinition("durable", Set.of(), 0f));
			assertEquals(2, StatusModifierRegistry.size());
		}
	}

	@Nested
	@DisplayName("Filtering Operations")
	class FilteringOperations {

		@Test
		@DisplayName("should find applicable by component tags")
		void shouldFindApplicableByComponentTags() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(id("tool"), id("weapon")), 0f));
			StatusModifierRegistry.register(createDefinition("durable", Set.of(id("armor")), 0f));
			StatusModifierRegistry.register(createDefinition("universal", Set.of(), 0f)); // No restriction

			List<StatusModifierDefinition> applicable = StatusModifierRegistry.findApplicable(Set.of(id("tool")));

			// Should find sharp (matches tool) and universal (no restriction)
			assertEquals(2, applicable.size());
		}

		@Test
		@DisplayName("should find applicable by component id")
		void shouldFindApplicableByComponentId() {
			StatusModifierDefinition specific = new SimpleStatusModifierDefinition(
					createModifier("special"),
					Set.of(),
					Set.of(id("iron_sword")),
					0f
			);
			StatusModifierDefinition universal = createDefinition("universal", Set.of(), 0f);

			StatusModifierRegistry.register(specific);
			StatusModifierRegistry.register(universal);

			List<StatusModifierDefinition> applicable = StatusModifierRegistry.findApplicableToId(id("iron_sword"));

			assertEquals(2, applicable.size());
		}

		@Test
		@DisplayName("should find random applicable with chance")
		void shouldFindRandomApplicableWithChance() {
			StatusModifierRegistry.register(createDefinition("common", Set.of(id("tool")), 0.5f));
			StatusModifierRegistry.register(createDefinition("rare", Set.of(id("tool")), 0.01f));
			StatusModifierRegistry.register(createDefinition("never", Set.of(id("tool")), 0f)); // No chance

			List<StatusModifierDefinition> applicable = StatusModifierRegistry.findRandomApplicable(Set.of(id("tool")));

			// Should find common and rare (non-zero chance), but not never
			assertEquals(2, applicable.size());
		}

		@Test
		@DisplayName("should find by priority range")
		void shouldFindByPriorityRange() {
			StatusModifierRegistry.register(new SimpleStatusModifierDefinition(
					SimpleStatusModifier.builder("forgero:low").priority(1).build(),
					Set.of(), Set.of(), 0f
			));
			StatusModifierRegistry.register(new SimpleStatusModifierDefinition(
					SimpleStatusModifier.builder("forgero:medium").priority(5).build(),
					Set.of(), Set.of(), 0f
			));
			StatusModifierRegistry.register(new SimpleStatusModifierDefinition(
					SimpleStatusModifier.builder("forgero:high").priority(10).build(),
					Set.of(), Set.of(), 0f
			));

			List<StatusModifierDefinition> found = StatusModifierRegistry.findByPriorityRange(3, 7);

			assertEquals(1, found.size());
			assertEquals(5, found.get(0).modifier().priority());
		}
	}

	@Nested
	@DisplayName("Lifecycle Operations")
	class LifecycleOperations {

		@Test
		@DisplayName("should clear all registrations")
		void shouldClearAllRegistrations() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));
			StatusModifierRegistry.register(createDefinition("durable", Set.of(), 0f));

			StatusModifierRegistry.clear();

			assertEquals(0, StatusModifierRegistry.size());
			assertTrue(StatusModifierRegistry.all().toList().isEmpty());
		}

		@Test
		@DisplayName("should refresh registry")
		void shouldRefreshRegistry() {
			StatusModifierRegistry.register(createDefinition("sharp", Set.of(), 0f));

			StatusModifierRegistry.refresh();

			assertEquals(0, StatusModifierRegistry.size());
		}
	}

	@Nested
	@DisplayName("Well-Known IDs")
	class WellKnownIds {

		@Test
		@DisplayName("should have BROKEN_ID constant")
		void shouldHaveBrokenIdConstant() {
			assertEquals(id("broken"), StatusModifierRegistry.BROKEN_ID);
		}

		@Test
		@DisplayName("should have UNBREAKABLE_ID constant")
		void shouldHaveUnbreakableIdConstant() {
			assertEquals(id("unbreakable"), StatusModifierRegistry.UNBREAKABLE_ID);
		}
	}
}
