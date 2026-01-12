package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BakedAttributes")
class BakedAttributesTest {

	private static final OpenIdentifier ATTACK_DAMAGE = DefaultAttributes.ATTACK_DAMAGE;
	private static final OpenIdentifier DURABILITY = DefaultAttributes.DURABILITY;
	private static final OpenIdentifier MINING_SPEED = DefaultAttributes.MINING_SPEED;

	@Nested
	@DisplayName("get()")
	class Get {

		@Test
		@DisplayName("returns precomputed for existing type")
		void returnsPrecomputedForExistingType() {
			PrecomputedAttribute expected = new PrecomputedAttribute(10.0f, List.of());
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, expected));

			PrecomputedAttribute result = baked.get(ATTACK_DAMAGE);

			assertSame(expected, result);
		}

		@Test
		@DisplayName("returns ZERO for missing type")
		void returnsZeroForMissingType() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			PrecomputedAttribute result = baked.get(DURABILITY);

			assertSame(PrecomputedAttribute.ZERO, result);
		}

		@Test
		@DisplayName("returns correct type when multiple exist")
		void returnsCorrectTypeWhenMultipleExist() {
			PrecomputedAttribute attackDamage = new PrecomputedAttribute(7.0f, List.of());
			PrecomputedAttribute durability = new PrecomputedAttribute(1200.0f, List.of());
			PrecomputedAttribute miningSpeed = new PrecomputedAttribute(6.0f, List.of());

			BakedAttributes baked = new BakedAttributes(Map.of(
					ATTACK_DAMAGE, attackDamage,
					DURABILITY, durability,
					MINING_SPEED, miningSpeed
			));

			assertEquals(7.0f, baked.get(ATTACK_DAMAGE).baseValue(), 0.001f);
			assertEquals(1200.0f, baked.get(DURABILITY).baseValue(), 0.001f);
			assertEquals(6.0f, baked.get(MINING_SPEED).baseValue(), 0.001f);
		}
	}

	@Nested
	@DisplayName("contains()")
	class Contains {

		@Test
		@DisplayName("returns true for existing type")
		void returnsTrueForExistingType() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			assertTrue(baked.contains(ATTACK_DAMAGE));
		}

		@Test
		@DisplayName("returns false for missing type")
		void returnsFalseForMissingType() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			assertFalse(baked.contains(DURABILITY));
		}

		@Test
		@DisplayName("returns false for empty baked")
		void returnsFalseForEmptyBaked() {
			assertFalse(BakedAttributes.EMPTY.contains(ATTACK_DAMAGE));
		}
	}

	@Nested
	@DisplayName("isEmpty()")
	class IsEmpty {

		@Test
		@DisplayName("returns true for EMPTY constant")
		void returnsTrueForEmptyConstant() {
			assertTrue(BakedAttributes.EMPTY.isEmpty());
		}

		@Test
		@DisplayName("returns true for empty map")
		void returnsTrueForEmptyMap() {
			BakedAttributes baked = new BakedAttributes(Map.of());

			assertTrue(baked.isEmpty());
		}

		@Test
		@DisplayName("returns false when attributes present")
		void returnsFalseWhenAttributesPresent() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			assertFalse(baked.isEmpty());
		}
	}

	@Nested
	@DisplayName("size()")
	class Size {

		@Test
		@DisplayName("returns zero for empty")
		void returnsZeroForEmpty() {
			assertEquals(0, BakedAttributes.EMPTY.size());
		}

		@Test
		@DisplayName("returns correct count for single type")
		void returnsCorrectCountForSingleType() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			assertEquals(1, baked.size());
		}

		@Test
		@DisplayName("returns correct count for multiple types")
		void returnsCorrectCountForMultipleTypes() {
			BakedAttributes baked = new BakedAttributes(Map.of(
					ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of()),
					DURABILITY, new PrecomputedAttribute(100.0f, List.of()),
					MINING_SPEED, new PrecomputedAttribute(5.0f, List.of())
			));

			assertEquals(3, baked.size());
		}
	}

	@Nested
	@DisplayName("EMPTY constant")
	class EmptyConstant {

		@Test
		@DisplayName("is empty")
		void isEmpty() {
			assertTrue(BakedAttributes.EMPTY.isEmpty());
		}

		@Test
		@DisplayName("has size zero")
		void hasSizeZero() {
			assertEquals(0, BakedAttributes.EMPTY.size());
		}

		@Test
		@DisplayName("returns ZERO for any type")
		void returnsZeroForAnyType() {
			assertSame(PrecomputedAttribute.ZERO, BakedAttributes.EMPTY.get(ATTACK_DAMAGE));
			assertSame(PrecomputedAttribute.ZERO, BakedAttributes.EMPTY.get(DURABILITY));
			assertSame(PrecomputedAttribute.ZERO, BakedAttributes.EMPTY.get(MINING_SPEED));
		}
	}

	@Nested
	@DisplayName("Immutability")
	class Immutability {

		@Test
		@DisplayName("map is defensively copied")
		void mapIsDefensivelyCopied() {
			Map<OpenIdentifier, PrecomputedAttribute> mutableMap = new HashMap<>();
			mutableMap.put(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of()));

			BakedAttributes baked = new BakedAttributes(mutableMap);

			// Modify original map
			mutableMap.clear();

			// BakedAttributes should still have the entry
			assertTrue(baked.contains(ATTACK_DAMAGE));
		}

		@Test
		@DisplayName("returned map is immutable")
		void returnedMapIsImmutable() {
			BakedAttributes baked = new BakedAttributes(Map.of(ATTACK_DAMAGE, new PrecomputedAttribute(10.0f, List.of())));

			assertThrows(UnsupportedOperationException.class, () ->
					baked.byType().clear()
			);
		}
	}
}
