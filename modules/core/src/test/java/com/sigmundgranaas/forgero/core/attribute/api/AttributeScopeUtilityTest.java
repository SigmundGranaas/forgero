package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.composition.AttributeScopeHandler;
import com.sigmundgranaas.forgero.core.attribute.composition.LocalScopeHandler;
import com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeScopeHandler;
import com.sigmundgranaas.forgero.core.attribute.composition.UpgradeScopeHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link AttributeScope} utility methods.
 * <p>
 * Tests all convenience methods for scope identification, handler retrieval,
 * and scope classification (composition vs filter scopes).
 * </p>
 */
@DisplayName("AttributeScope Utility Methods Tests")
class AttributeScopeUtilityTest {

	private static final OpenIdentifier RANDOM_IDENTIFIER = new OpenIdentifier("forgero", "random/unknown");
	private static final OpenIdentifier CUSTOM_SCOPE = new OpenIdentifier("forgero", "contexts/custom");

	@Nested
	@DisplayName("isPartComposite() tests")
	class IsPartCompositeTests {

		@Test
		@DisplayName("Returns true for PART_COMPOSITE scope")
		void returnsTrueForPartComposite() {
			assertTrue(AttributeScope.isPartComposite(AttributeScope.PART_COMPOSITE),
					"Should return true for PART_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for EQUIPMENT_COMPOSITE scope")
		void returnsFalseForEquipmentComposite() {
			assertFalse(AttributeScope.isPartComposite(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return false for EQUIPMENT_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for UPGRADE scope")
		void returnsFalseForUpgrade() {
			assertFalse(AttributeScope.isPartComposite(AttributeScope.UPGRADE),
					"Should return false for UPGRADE scope");
		}

		@Test
		@DisplayName("Returns false for LOCAL scope")
		void returnsFalseForLocal() {
			assertFalse(AttributeScope.isPartComposite(AttributeScope.LOCAL),
					"Should return false for LOCAL scope");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isPartComposite(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isPartComposite(null),
					"Should return false for null identifier");
		}
	}

	@Nested
	@DisplayName("isEquipmentComposite() tests")
	class IsEquipmentCompositeTests {

		@Test
		@DisplayName("Returns true for EQUIPMENT_COMPOSITE scope")
		void returnsTrueForEquipmentComposite() {
			assertTrue(AttributeScope.isEquipmentComposite(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return true for EQUIPMENT_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for PART_COMPOSITE scope")
		void returnsFalseForPartComposite() {
			assertFalse(AttributeScope.isEquipmentComposite(AttributeScope.PART_COMPOSITE),
					"Should return false for PART_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for UPGRADE scope")
		void returnsFalseForUpgrade() {
			assertFalse(AttributeScope.isEquipmentComposite(AttributeScope.UPGRADE),
					"Should return false for UPGRADE scope");
		}

		@Test
		@DisplayName("Returns false for LOCAL scope")
		void returnsFalseForLocal() {
			assertFalse(AttributeScope.isEquipmentComposite(AttributeScope.LOCAL),
					"Should return false for LOCAL scope");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isEquipmentComposite(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isEquipmentComposite(null),
					"Should return false for null identifier");
		}
	}

	@Nested
	@DisplayName("isUpgrade() tests")
	class IsUpgradeTests {

		@Test
		@DisplayName("Returns true for UPGRADE scope")
		void returnsTrueForUpgrade() {
			assertTrue(AttributeScope.isUpgrade(AttributeScope.UPGRADE),
					"Should return true for UPGRADE scope");
		}

		@Test
		@DisplayName("Returns false for PART_COMPOSITE scope")
		void returnsFalseForPartComposite() {
			assertFalse(AttributeScope.isUpgrade(AttributeScope.PART_COMPOSITE),
					"Should return false for PART_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for EQUIPMENT_COMPOSITE scope")
		void returnsFalseForEquipmentComposite() {
			assertFalse(AttributeScope.isUpgrade(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return false for EQUIPMENT_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for LOCAL scope")
		void returnsFalseForLocal() {
			assertFalse(AttributeScope.isUpgrade(AttributeScope.LOCAL),
					"Should return false for LOCAL scope");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isUpgrade(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isUpgrade(null),
					"Should return false for null identifier");
		}
	}

	@Nested
	@DisplayName("isLocal() tests")
	class IsLocalTests {

		@Test
		@DisplayName("Returns true for LOCAL scope")
		void returnsTrueForLocal() {
			assertTrue(AttributeScope.isLocal(AttributeScope.LOCAL),
					"Should return true for LOCAL scope");
		}

		@Test
		@DisplayName("Returns false for PART_COMPOSITE scope")
		void returnsFalseForPartComposite() {
			assertFalse(AttributeScope.isLocal(AttributeScope.PART_COMPOSITE),
					"Should return false for PART_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for EQUIPMENT_COMPOSITE scope")
		void returnsFalseForEquipmentComposite() {
			assertFalse(AttributeScope.isLocal(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return false for EQUIPMENT_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for UPGRADE scope")
		void returnsFalseForUpgrade() {
			assertFalse(AttributeScope.isLocal(AttributeScope.UPGRADE),
					"Should return false for UPGRADE scope");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isLocal(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isLocal(null),
					"Should return false for null identifier");
		}
	}

	@Nested
	@DisplayName("isDefault() tests")
	class IsDefaultTests {

		@Test
		@DisplayName("Returns true for empty Optional")
		void returnsTrueForEmptyOptional() {
			assertTrue(AttributeScope.isDefault(Optional.empty()),
					"Should return true for empty Optional (no scope = default)");
		}

		@Test
		@DisplayName("Returns false for Optional with PART_COMPOSITE")
		void returnsFalseForPartComposite() {
			assertFalse(AttributeScope.isDefault(Optional.of(AttributeScope.PART_COMPOSITE)),
					"Should return false for Optional with PART_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for Optional with EQUIPMENT_COMPOSITE")
		void returnsFalseForEquipmentComposite() {
			assertFalse(AttributeScope.isDefault(Optional.of(AttributeScope.EQUIPMENT_COMPOSITE)),
					"Should return false for Optional with EQUIPMENT_COMPOSITE scope");
		}

		@Test
		@DisplayName("Returns false for Optional with UPGRADE")
		void returnsFalseForUpgrade() {
			assertFalse(AttributeScope.isDefault(Optional.of(AttributeScope.UPGRADE)),
					"Should return false for Optional with UPGRADE scope");
		}

		@Test
		@DisplayName("Returns false for Optional with LOCAL")
		void returnsFalseForLocal() {
			assertFalse(AttributeScope.isDefault(Optional.of(AttributeScope.LOCAL)),
					"Should return false for Optional with LOCAL scope");
		}

		@Test
		@DisplayName("Returns false for Optional with custom scope")
		void returnsFalseForCustomScope() {
			assertFalse(AttributeScope.isDefault(Optional.of(CUSTOM_SCOPE)),
					"Should return false for Optional with custom scope");
		}
	}

	@Nested
	@DisplayName("getHandler() tests")
	class GetHandlerTests {

		@Test
		@DisplayName("Returns PartCompositeScopeHandler for PART_COMPOSITE")
		void returnsPartCompositeHandler() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(AttributeScope.PART_COMPOSITE);

			assertTrue(handler.isPresent(), "Should return a handler for PART_COMPOSITE");
			assertSame(PartCompositeScopeHandler.INSTANCE, handler.get(),
					"Should return PartCompositeScopeHandler.INSTANCE");
		}

		@Test
		@DisplayName("Returns UpgradeScopeHandler for UPGRADE")
		void returnsUpgradeHandler() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(AttributeScope.UPGRADE);

			assertTrue(handler.isPresent(), "Should return a handler for UPGRADE");
			assertSame(UpgradeScopeHandler.INSTANCE, handler.get(),
					"Should return UpgradeScopeHandler.INSTANCE");
		}

		@Test
		@DisplayName("Returns LocalScopeHandler for LOCAL")
		void returnsLocalHandler() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(AttributeScope.LOCAL);

			assertTrue(handler.isPresent(), "Should return a handler for LOCAL");
			assertSame(LocalScopeHandler.INSTANCE, handler.get(),
					"Should return LocalScopeHandler.INSTANCE");
		}

		@Test
		@DisplayName("Returns empty for EQUIPMENT_COMPOSITE")
		void returnsEmptyForEquipmentComposite() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(AttributeScope.EQUIPMENT_COMPOSITE);

			assertFalse(handler.isPresent(),
					"Should return empty for EQUIPMENT_COMPOSITE (no handler defined)");
		}

		@Test
		@DisplayName("Returns empty for random identifier")
		void returnsEmptyForRandomIdentifier() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(RANDOM_IDENTIFIER);

			assertFalse(handler.isPresent(), "Should return empty for unknown scope");
		}

		@Test
		@DisplayName("Returns empty for custom scope")
		void returnsEmptyForCustomScope() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(CUSTOM_SCOPE);

			assertFalse(handler.isPresent(), "Should return empty for custom scope");
		}

		@Test
		@DisplayName("Returns empty for null identifier")
		void returnsEmptyForNull() {
			Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(null);

			assertFalse(handler.isPresent(), "Should return empty for null identifier");
		}

		@Test
		@DisplayName("Returns singleton instances")
		void returnsSingletonInstances() {
			// Verify that multiple calls return the same instance
			Optional<AttributeScopeHandler> handler1 = AttributeScope.getHandler(AttributeScope.LOCAL);
			Optional<AttributeScopeHandler> handler2 = AttributeScope.getHandler(AttributeScope.LOCAL);

			assertTrue(handler1.isPresent() && handler2.isPresent(),
					"Both calls should return a handler");
			assertSame(handler1.get(), handler2.get(),
					"Multiple calls should return the same singleton instance");
		}
	}

	@Nested
	@DisplayName("isCompositionScope() tests")
	class IsCompositionScopeTests {

		@Test
		@DisplayName("Returns true for PART_COMPOSITE")
		void returnsTrueForPartComposite() {
			assertTrue(AttributeScope.isCompositionScope(AttributeScope.PART_COMPOSITE),
					"Should return true for PART_COMPOSITE (requires composition handling)");
		}

		@Test
		@DisplayName("Returns true for EQUIPMENT_COMPOSITE")
		void returnsTrueForEquipmentComposite() {
			assertTrue(AttributeScope.isCompositionScope(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return true for EQUIPMENT_COMPOSITE (requires composition handling)");
		}

		@Test
		@DisplayName("Returns false for UPGRADE")
		void returnsFalseForUpgrade() {
			assertFalse(AttributeScope.isCompositionScope(AttributeScope.UPGRADE),
					"Should return false for UPGRADE (filter scope, not composition)");
		}

		@Test
		@DisplayName("Returns false for LOCAL")
		void returnsFalseForLocal() {
			assertFalse(AttributeScope.isCompositionScope(AttributeScope.LOCAL),
					"Should return false for LOCAL (filter scope, not composition)");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isCompositionScope(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isCompositionScope(null),
					"Should return false for null identifier");
		}

		@Test
		@DisplayName("Identifies both composition scopes")
		void identifiesBothCompositionScopes() {
			// Verify that both PART_COMPOSITE and EQUIPMENT_COMPOSITE are identified
			long compositionScopeCount = java.util.stream.Stream.of(
					AttributeScope.PART_COMPOSITE,
					AttributeScope.EQUIPMENT_COMPOSITE,
					AttributeScope.UPGRADE,
					AttributeScope.LOCAL
			).filter(AttributeScope::isCompositionScope).count();

			assertEquals(2, compositionScopeCount,
					"Exactly 2 composition scopes should be identified");
		}
	}

	@Nested
	@DisplayName("isFilterScope() tests")
	class IsFilterScopeTests {

		@Test
		@DisplayName("Returns true for LOCAL")
		void returnsTrueForLocal() {
			assertTrue(AttributeScope.isFilterScope(AttributeScope.LOCAL),
					"Should return true for LOCAL (simple include/exclude filter)");
		}

		@Test
		@DisplayName("Returns true for UPGRADE")
		void returnsTrueForUpgrade() {
			assertTrue(AttributeScope.isFilterScope(AttributeScope.UPGRADE),
					"Should return true for UPGRADE (simple include/exclude filter)");
		}

		@Test
		@DisplayName("Returns false for PART_COMPOSITE")
		void returnsFalseForPartComposite() {
			assertFalse(AttributeScope.isFilterScope(AttributeScope.PART_COMPOSITE),
					"Should return false for PART_COMPOSITE (composition scope, not filter)");
		}

		@Test
		@DisplayName("Returns false for EQUIPMENT_COMPOSITE")
		void returnsFalseForEquipmentComposite() {
			assertFalse(AttributeScope.isFilterScope(AttributeScope.EQUIPMENT_COMPOSITE),
					"Should return false for EQUIPMENT_COMPOSITE (composition scope, not filter)");
		}

		@Test
		@DisplayName("Returns false for random identifier")
		void returnsFalseForRandomIdentifier() {
			assertFalse(AttributeScope.isFilterScope(RANDOM_IDENTIFIER),
					"Should return false for unknown identifier");
		}

		@Test
		@DisplayName("Returns false for null identifier")
		void returnsFalseForNull() {
			assertFalse(AttributeScope.isFilterScope(null),
					"Should return false for null identifier");
		}

		@Test
		@DisplayName("Identifies both filter scopes")
		void identifiesBothFilterScopes() {
			// Verify that both LOCAL and UPGRADE are identified
			long filterScopeCount = java.util.stream.Stream.of(
					AttributeScope.PART_COMPOSITE,
					AttributeScope.EQUIPMENT_COMPOSITE,
					AttributeScope.UPGRADE,
					AttributeScope.LOCAL
			).filter(AttributeScope::isFilterScope).count();

			assertEquals(2, filterScopeCount,
					"Exactly 2 filter scopes should be identified");
		}
	}

	@Nested
	@DisplayName("Scope classification tests")
	class ScopeClassificationTests {

		@Test
		@DisplayName("Composition and filter scopes are mutually exclusive")
		void compositionAndFilterAreMutuallyExclusive() {
			// Verify no scope is both composition AND filter
			java.util.List<OpenIdentifier> allScopes = java.util.List.of(
					AttributeScope.PART_COMPOSITE,
					AttributeScope.EQUIPMENT_COMPOSITE,
					AttributeScope.UPGRADE,
					AttributeScope.LOCAL
			);

			for (OpenIdentifier scope : allScopes) {
				boolean isComposition = AttributeScope.isCompositionScope(scope);
				boolean isFilter = AttributeScope.isFilterScope(scope);

				assertFalse(isComposition && isFilter,
						"Scope " + scope + " should not be both composition AND filter");
			}
		}

		@Test
		@DisplayName("All known scopes are either composition or filter")
		void allKnownScopesAreClassified() {
			// Verify every known scope is classified as either composition or filter
			java.util.List<OpenIdentifier> knownScopes = java.util.List.of(
					AttributeScope.PART_COMPOSITE,
					AttributeScope.EQUIPMENT_COMPOSITE,
					AttributeScope.UPGRADE,
					AttributeScope.LOCAL
			);

			for (OpenIdentifier scope : knownScopes) {
				boolean isComposition = AttributeScope.isCompositionScope(scope);
				boolean isFilter = AttributeScope.isFilterScope(scope);

				assertTrue(isComposition || isFilter,
						"Scope " + scope + " should be classified as either composition or filter");
			}
		}
	}

	@Nested
	@DisplayName("Scope handler correspondence tests")
	class ScopeHandlerCorrespondenceTests {

		@Test
		@DisplayName("All filter scopes have handlers")
		void allFilterScopesHaveHandlers() {
			// Verify every filter scope has a corresponding handler
			java.util.List<OpenIdentifier> filterScopes = java.util.List.of(
					AttributeScope.LOCAL,
					AttributeScope.UPGRADE
			);

			for (OpenIdentifier scope : filterScopes) {
				Optional<AttributeScopeHandler> handler = AttributeScope.getHandler(scope);
				assertTrue(handler.isPresent(),
						"Filter scope " + scope + " should have a handler");
			}
		}

		@Test
		@DisplayName("PART_COMPOSITE has handler but EQUIPMENT_COMPOSITE does not")
		void compositionScopeHandlerAvailability() {
			// PART_COMPOSITE has handler
			assertTrue(AttributeScope.getHandler(AttributeScope.PART_COMPOSITE).isPresent(),
					"PART_COMPOSITE should have a handler");

			// EQUIPMENT_COMPOSITE does not have handler (handled differently)
			assertFalse(AttributeScope.getHandler(AttributeScope.EQUIPMENT_COMPOSITE).isPresent(),
					"EQUIPMENT_COMPOSITE should not have a handler (composition handled differently)");
		}
	}

	@Nested
	@DisplayName("Edge cases and robustness tests")
	class EdgeCaseTests {

		@Test
		@DisplayName("Methods handle null gracefully")
		void methodsHandleNullGracefully() {
			assertFalse(AttributeScope.isPartComposite(null));
			assertFalse(AttributeScope.isEquipmentComposite(null));
			assertFalse(AttributeScope.isUpgrade(null));
			assertFalse(AttributeScope.isLocal(null));
			assertFalse(AttributeScope.isCompositionScope(null));
			assertFalse(AttributeScope.isFilterScope(null));
			assertFalse(AttributeScope.getHandler(null).isPresent());
		}

		@Test
		@DisplayName("Scope constant equality is consistent")
		void scopeConstantEqualityIsConsistent() {
			// Verify that scope constants are properly equal to themselves
			assertEquals(AttributeScope.PART_COMPOSITE, AttributeScope.PART_COMPOSITE);
			assertEquals(AttributeScope.EQUIPMENT_COMPOSITE, AttributeScope.EQUIPMENT_COMPOSITE);
			assertEquals(AttributeScope.UPGRADE, AttributeScope.UPGRADE);
			assertEquals(AttributeScope.LOCAL, AttributeScope.LOCAL);
		}

		@Test
		@DisplayName("Scope constants have expected namespace and path")
		void scopeConstantsHaveExpectedFormat() {
			// Verify scope constants use "forgero" namespace and "scope/" prefix
			assertEquals("forgero", AttributeScope.PART_COMPOSITE.namespace());
			assertEquals("scope/part-composite", AttributeScope.PART_COMPOSITE.path());

			assertEquals("forgero", AttributeScope.EQUIPMENT_COMPOSITE.namespace());
			assertEquals("scope/equipment-composite", AttributeScope.EQUIPMENT_COMPOSITE.path());

			assertEquals("forgero", AttributeScope.UPGRADE.namespace());
			assertEquals("scope/upgrade", AttributeScope.UPGRADE.path());

			assertEquals("forgero", AttributeScope.LOCAL.namespace());
			assertEquals("scope/local", AttributeScope.LOCAL.path());
		}
	}
}
