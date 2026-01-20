package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemComparisonApiImpl API contract.
 * <p>
 * These tests verify the documented behavior of the ItemComparisonApi:
 * - All methods must handle null ItemStack gracefully
 * - Comparison methods return false for null/invalid inputs
 * <p>
 * Full integration tests with real Forgero items require GameTest.
 */
@DisplayName("ItemComparisonApiImpl API Contract")
class ItemComparisonApiImplTest {

	private ItemComparisonApiImpl api;

	@BeforeEach
	void setUp() {
		api = new ItemComparisonApiImpl(new StubComponentConverter());
	}

	@Nested
	@DisplayName("API Contract: Null Safety")
	class NullSafetyContract {

		@Test
		@DisplayName("isSameType returns false for null inputs")
		void isSameTypeReturnsFalseForNull() {
			assertFalse(api.isSameType(null, null), "Should return false when both stacks are null");
		}

		@Test
		@DisplayName("areSimilar returns false for null inputs")
		void areSimilarReturnsFalseForNull() {
			assertFalse(api.areSimilar(null, null), "Should return false when both stacks are null");
		}
	}

	// ========== Stub implementation ==========

	private static class StubComponentConverter implements ComponentConverter {
		@Override public Optional<Component> toComponent(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Item item) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Identifier itemId) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(Component component) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Item> toItem(Component component) { return Optional.empty(); }
		@Override public Optional<Item> toItem(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Identifier> toItemId(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(Item item) { return Optional.empty(); }
	}
}
