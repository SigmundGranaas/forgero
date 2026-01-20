package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ItemRegistrar - the component that handles registration of Forgero items.
 *
 * <p><strong>IMPORTANT LIMITATION:</strong> ItemRegistrar has a static initializer that references
 * Minecraft's ItemGroups registry. This means most tests in this file will fail with
 * {@code NoClassDefFoundError: Could not initialize class} when run as unit tests without
 * a full Minecraft runtime environment.
 *
 * <p><strong>To run full tests:</strong> They must be executed in a Minecraft
 * GameTest environment with full registry initialization.
 *
 * <p>The record type tests can run because they don't trigger the static initializer.
 */
class ItemRegistrarTest {

	@Nested
	@DisplayName("Constructor tests")
	class ConstructorTests {

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("should accept ComponentRegistry and Logger without throwing")
		void constructorAcceptsValidArguments() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("should store ComponentRegistry for later use")
		void constructorStoresComponentRegistry() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}
	}

	@Nested
	@DisplayName("Registration callback tests")
	class RegistrationCallbackTests {

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("addRegistrationCallback should store callback")
		void addRegistrationCallbackStoresCallback() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("should allow multiple callbacks to be added")
		void shouldAllowMultipleCallbacks() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}
	}

	@Nested
	@DisplayName("Record type tests - PendingItemGroupRegistration")
	class PendingItemGroupRegistrationTests {

		// Note: These tests use null for Item because creating real Item instances
		// requires Minecraft runtime. The record itself works fine with null.

		@Test
		@DisplayName("should store groupId")
		void shouldStoreGroupId() {
			String groupId = "forgero:combat";

			ItemRegistrar.PendingItemGroupRegistration registration =
					new ItemRegistrar.PendingItemGroupRegistration(null, groupId);

			assertEquals(groupId, registration.groupId());
		}

		@Test
		@DisplayName("should allow null groupId")
		void shouldAllowNullGroupId() {
			ItemRegistrar.PendingItemGroupRegistration registration =
					new ItemRegistrar.PendingItemGroupRegistration(null, null);

			assertNull(registration.groupId());
		}

		@Test
		@DisplayName("should implement equals based on components")
		void shouldImplementEqualsBasedOnComponents() {
			String groupId = "forgero:tools";

			ItemRegistrar.PendingItemGroupRegistration reg1 =
					new ItemRegistrar.PendingItemGroupRegistration(null, groupId);
			ItemRegistrar.PendingItemGroupRegistration reg2 =
					new ItemRegistrar.PendingItemGroupRegistration(null, groupId);

			assertEquals(reg1, reg2);
		}

		@Test
		@DisplayName("should implement hashCode consistently")
		void shouldImplementHashCodeConsistently() {
			String groupId = "minecraft:combat";

			ItemRegistrar.PendingItemGroupRegistration registration =
					new ItemRegistrar.PendingItemGroupRegistration(null, groupId);

			int hash1 = registration.hashCode();
			int hash2 = registration.hashCode();

			assertEquals(hash1, hash2);
		}

		@Test
		@DisplayName("should not equal when groupId differs")
		void shouldNotEqualWhenGroupIdDiffers() {
			ItemRegistrar.PendingItemGroupRegistration reg1 =
					new ItemRegistrar.PendingItemGroupRegistration(null, "forgero:tools");
			ItemRegistrar.PendingItemGroupRegistration reg2 =
					new ItemRegistrar.PendingItemGroupRegistration(null, "forgero:combat");

			assertNotEquals(reg1, reg2);
		}
	}

	@Nested
	@DisplayName("Record type tests - RegisteredItem")
	class RegisteredItemTests {

		@Test
		@DisplayName("should store id correctly")
		void shouldStoreIdCorrectly() {
			Identifier id = new Identifier("forgero", "iron_pickaxe");

			ItemRegistrar.RegisteredItem registeredItem =
					new ItemRegistrar.RegisteredItem(id, null, null, null);

			assertSame(id, registeredItem.id());
		}

		@Test
		@DisplayName("should allow null fields")
		void shouldAllowNullFields() {
			Identifier id = new Identifier("forgero", "test");

			ItemRegistrar.RegisteredItem registeredItem =
					new ItemRegistrar.RegisteredItem(id, null, null, null);

			assertNotNull(registeredItem);
			assertSame(id, registeredItem.id());
			assertNull(registeredItem.item());
			assertNull(registeredItem.component());
			assertNull(registeredItem.createData());
		}

		@Test
		@DisplayName("should implement equals based on all components")
		void shouldImplementEqualsBasedOnAllComponents() {
			Identifier id = new Identifier("forgero", "iron_pickaxe");

			ItemRegistrar.RegisteredItem item1 =
					new ItemRegistrar.RegisteredItem(id, null, null, null);
			ItemRegistrar.RegisteredItem item2 =
					new ItemRegistrar.RegisteredItem(id, null, null, null);

			assertEquals(item1, item2);
		}

		@Test
		@DisplayName("should not equal when id differs")
		void shouldNotEqualWhenIdDiffers() {
			Identifier id1 = new Identifier("forgero", "iron_pickaxe");
			Identifier id2 = new Identifier("forgero", "diamond_pickaxe");

			ItemRegistrar.RegisteredItem item1 =
					new ItemRegistrar.RegisteredItem(id1, null, null, null);
			ItemRegistrar.RegisteredItem item2 =
					new ItemRegistrar.RegisteredItem(id2, null, null, null);

			assertNotEquals(item1, item2);
		}

		@Test
		@DisplayName("should implement hashCode consistently")
		void shouldImplementHashCodeConsistently() {
			Identifier id = new Identifier("forgero", "diamond_sword");

			ItemRegistrar.RegisteredItem registeredItem =
					new ItemRegistrar.RegisteredItem(id, null, null, null);

			int hash1 = registeredItem.hashCode();
			int hash2 = registeredItem.hashCode();

			assertEquals(hash1, hash2);
		}

		@Test
		@DisplayName("toString should include relevant information")
		void toStringShouldIncludeRelevantInformation() {
			Identifier id = new Identifier("forgero", "iron_pickaxe");

			ItemRegistrar.RegisteredItem registeredItem =
					new ItemRegistrar.RegisteredItem(id, null, null, null);

			String toString = registeredItem.toString();

			assertTrue(toString.contains("RegisteredItem"));
			assertTrue(toString.contains("forgero:iron_pickaxe") || toString.contains("iron_pickaxe"));
		}
	}

	@Nested
	@DisplayName("Registration behavior tests")
	class RegistrationBehaviorTests {

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("registerItems should return empty list for empty input")
		void registerItemsShouldReturnEmptyListForEmptyInput() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}

		@Test
		@Disabled("ItemRegistrar static initializer requires Minecraft runtime - test in GameTest")
		@DisplayName("registerItems should skip items with null CreateData")
		void registerItemsShouldSkipItemsWithNullCreateData() {
			// This test requires Minecraft's ItemGroups to be initialized
			// Should be implemented as a GameTest
		}
	}

	@Nested
	@DisplayName("Integration scenarios (Disabled - require Minecraft runtime)")
	class IntegrationScenarios {

		@Test
		@Disabled("Requires Minecraft Registry.register to be initialized")
		@DisplayName("Full registration flow with valid inputs")
		void fullRegistrationFlowWithValidInputs() {
			// This test requires:
			// - Minecraft Registry to be initialized
			// - Valid Item instances
			// - Proper game environment
			// Should be implemented as a GameTest
		}

		@Test
		@Disabled("Requires Minecraft Registry.register to be initialized")
		@DisplayName("Registration with callbacks modifying items")
		void registrationWithCallbacksModifyingItems() {
			// This test requires:
			// - Full Minecraft runtime
			// - ItemRegistrationCallback that can modify items
			// Should be implemented as a GameTest
		}
	}

	/**
	 * Minimal ComponentRegistry implementation for testing.
	 */
	private static class TestComponentRegistry implements ComponentRegistry {
		@Override
		public Optional<Component> get(OpenIdentifier id) {
			return Optional.empty();
		}

		@Override
		public List<Component> all() {
			return Collections.emptyList();
		}

		@Override
		public Builder toBuilder() {
			return ComponentRegistry.builder();
		}
	}
}
