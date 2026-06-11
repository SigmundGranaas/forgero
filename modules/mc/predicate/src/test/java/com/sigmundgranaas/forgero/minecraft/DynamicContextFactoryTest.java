package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.DynamicContextFactory;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DynamicContextFactory.
 *
 * Note: Tests involving actual Entity instances require the full Minecraft
 * bootstrap and are best tested via GameTests. These unit tests cover
 * null-safety and basic contract validation.
 */
class DynamicContextFactoryTest implements Bootstrapped {

	@Nested
	@DisplayName("fromEntity() Tests")
	class FromEntityTests {

		@Test
		@DisplayName("fromEntity(null) returns empty context")
		void fromEntityNullReturnsEmpty() {
			DynamicContext context = DynamicContextFactory.fromEntity(null);

			assertNotNull(context, "Should return non-null context");
			assertTrue(context.get(MinecraftContextKeys.SOURCE_ENTITY).isEmpty(),
					"Should not have source entity");
			assertTrue(context.get(MinecraftContextKeys.WORLD).isEmpty(),
					"Should not have world");
		}

		@Test
		@DisplayName("fromEntity returns same result as DynamicContext.empty() for null")
		void fromEntityNullMatchesEmptyContext() {
			DynamicContext fromNull = DynamicContextFactory.fromEntity(null);
			DynamicContext empty = DynamicContext.empty();

			// Both should be empty contexts
			assertEquals(
					empty.get(MinecraftContextKeys.SOURCE_ENTITY).isPresent(),
					fromNull.get(MinecraftContextKeys.SOURCE_ENTITY).isPresent(),
					"Both should be empty for SOURCE_ENTITY"
			);
		}
	}

	@Nested
	@DisplayName("fromEntities() Tests")
	class FromEntitiesTests {

		@Test
		@DisplayName("fromEntities(null, null) returns empty context")
		void fromEntitiesBothNullReturnsEmpty() {
			DynamicContext context = DynamicContextFactory.fromEntities(null, null);

			assertNotNull(context, "Should return non-null context");
			assertTrue(context.get(MinecraftContextKeys.SOURCE_ENTITY).isEmpty(),
					"Should not have source entity");
			assertTrue(context.get(MinecraftContextKeys.TARGET_ENTITY).isEmpty(),
					"Should not have target entity");
			assertTrue(context.get(MinecraftContextKeys.WORLD).isEmpty(),
					"Should not have world");
		}

		@Test
		@DisplayName("fromEntities returns same result as DynamicContext.empty() for both null")
		void fromEntitiesNullMatchesEmptyContext() {
			DynamicContext fromNulls = DynamicContextFactory.fromEntities(null, null);
			DynamicContext empty = DynamicContext.empty();

			assertEquals(
					empty.get(MinecraftContextKeys.SOURCE_ENTITY).isPresent(),
					fromNulls.get(MinecraftContextKeys.SOURCE_ENTITY).isPresent()
			);
			assertEquals(
					empty.get(MinecraftContextKeys.TARGET_ENTITY).isPresent(),
					fromNulls.get(MinecraftContextKeys.TARGET_ENTITY).isPresent()
			);
		}
	}

	@Nested
	@DisplayName("Contract Validation")
	class ContractTests {

		@Test
		@DisplayName("Factory methods are null-safe (no exceptions)")
		void factoryMethodsAreNullSafe() {
			// These should never throw
			assertDoesNotThrow(() -> DynamicContextFactory.fromEntity(null));
			assertDoesNotThrow(() -> DynamicContextFactory.fromEntities(null, null));
		}

		@Test
		@DisplayName("Factory always returns non-null context")
		void factoryAlwaysReturnsNonNull() {
			assertNotNull(DynamicContextFactory.fromEntity(null));
			assertNotNull(DynamicContextFactory.fromEntities(null, null));
		}
	}
}
