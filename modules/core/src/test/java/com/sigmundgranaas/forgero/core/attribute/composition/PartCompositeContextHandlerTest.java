package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PartCompositeContextHandler}.
 *
 * <p>These tests verify the intersection composition logic:</p>
 * <ul>
 *   <li>Requires base + multiplier from DIFFERENT sources</li>
 *   <li>Computes: sum(bases) × product(multipliers)</li>
 *   <li>Excludes attributes missing either component</li>
 * </ul>
 */
@DisplayName("PartCompositeContextHandler Tests")
class PartCompositeContextHandlerTest {

	private PartCompositeContextHandler handler;

	@BeforeEach
	void setUp() {
		handler = PartCompositeContextHandler.INSTANCE;
	}

	@Test
	@DisplayName("Handler has correct context ID")
	void contextIdIsPartComposite() {
		assertEquals(AttributeContext.PART_COMPOSITE, handler.contextId());
	}

	@Nested
	@DisplayName("Empty and Invalid Input")
	class EmptyInputTests {

		@Test
		@DisplayName("Empty sources returns empty list")
		void emptySources() {
			List<Attribute> result = handler.compose(Map.of());
			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("Single source returns empty (no intersection possible)")
		void singleSource() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = Map.of(
					"material", List.of(base)
			);

			List<Attribute> result = handler.compose(sources);
			assertTrue(result.isEmpty(), "Single source should not produce output");
		}

		@Test
		@DisplayName("Only bases from same source returns empty")
		void onlyBasesFromSameSource() {
			SimpleAttribute base1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute base2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					2f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = Map.of(
					"material", List.of(base1, base2)
			);

			List<Attribute> result = handler.compose(sources);
			assertTrue(result.isEmpty(), "Bases from same source should not compose");
		}
	}

	@Nested
	@DisplayName("Basic Composition")
	class BasicCompositionTests {

		@Test
		@DisplayName("Base from material × multiplier from shape = composed value")
		void materialBaseTimesShapeMultiplier() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					1.5f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(base));
			sources.put("shape", List.of(multiplier));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size(), "Should produce one composed attribute");
			Attribute composed = result.get(0);
			assertEquals(DefaultAttributes.ATTACK_DAMAGE, composed.type());
			assertEquals(6f, composed.value(), 0.001f, "4 × 1.5 = 6");
		}

		@Test
		@DisplayName("Identity multiplier (×1.0) passes through base value")
		void identityMultiplierPassesThrough() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					6f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					1.0f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(base));
			sources.put("schematic", List.of(multiplier));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertEquals(6f, result.get(0).value(), 0.001f, "6 × 1.0 = 6");
		}
	}

	@Nested
	@DisplayName("Intersection Filtering")
	class IntersectionTests {

		@Test
		@DisplayName("Attribute type without multiplier is excluded")
		void noMultiplierExcluded() {
			SimpleAttribute attackBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			// Only durability has a multiplier, not attack_damage
			SimpleAttribute durabilityMultiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					1.0f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(attackBase));
			sources.put("shape", List.of(durabilityMultiplier));

			List<Attribute> result = handler.compose(sources);

			// attack_damage should be excluded (no multiplier)
			// durability should also be excluded (no base)
			assertTrue(result.isEmpty(),
					"Attributes without matching base+multiplier pair should be excluded");
		}

		@Test
		@DisplayName("Handle schematic filters out attack_damage")
		void handleFiltersAttackDamage() {
			// Material provides attack_damage and durability bases
			SimpleAttribute attackBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute durabilityBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					240f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			// Handle only provides durability multiplier (not attack_damage)
			SimpleAttribute durabilityMult = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					1.0f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("iron_material", List.of(attackBase, durabilityBase));
			sources.put("handle_schematic", List.of(durabilityMult));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size(), "Only durability should compose");
			assertEquals(DefaultAttributes.DURABILITY, result.get(0).type());
			assertEquals(240f, result.get(0).value(), 0.001f);
		}
	}

	@Nested
	@DisplayName("Multiple Values")
	class MultipleValuesTests {

		@Test
		@DisplayName("Multiple bases sum together before multiplication")
		void multipleBasesSummed() {
			SimpleAttribute base1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					200f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute base2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					1.2f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material1", List.of(base1));
			sources.put("material2", List.of(base2));
			sources.put("shape", List.of(multiplier));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			// (200 + 50) × 1.2 = 300
			assertEquals(300f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Multiple multipliers multiply together")
		void multipleMultipliersProduct() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute mult1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					1.5f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute mult2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					2.0f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(base));
			sources.put("shape1", List.of(mult1));
			sources.put("shape2", List.of(mult2));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			// 4 × 1.5 × 2.0 = 12
			assertEquals(12f, result.get(0).value(), 0.001f);
		}
	}

	@Nested
	@DisplayName("Multiple Attribute Types")
	class MultipleTypesTests {

		@Test
		@DisplayName("Different attribute types compose independently")
		void independentComposition() {
			// Material provides multiple attributes
			SimpleAttribute attackBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute miningBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					6f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute durabilityBase = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					240f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			// Sword blade only provides attack and durability multipliers (not mining)
			SimpleAttribute attackMult = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					1.5f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute durabilityMult = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					1.0f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("iron_material", List.of(attackBase, miningBase, durabilityBase));
			sources.put("sword_blade", List.of(attackMult, durabilityMult));

			List<Attribute> result = handler.compose(sources);

			// mining_speed should be excluded (no multiplier in sword blade)
			assertEquals(2, result.size(), "Only attack_damage and durability should compose");

			Map<OpenIdentifier, Float> resultMap = new HashMap<>();
			for (Attribute attr : result) {
				resultMap.put(attr.type(), attr.value());
			}

			assertEquals(6f, resultMap.get(DefaultAttributes.ATTACK_DAMAGE), 0.001f, "4 × 1.5 = 6");
			assertEquals(240f, resultMap.get(DefaultAttributes.DURABILITY), 0.001f, "240 × 1.0 = 240");
			assertFalse(resultMap.containsKey(DefaultAttributes.MINING_SPEED), "mining_speed should be excluded");
		}
	}

	@Nested
	@DisplayName("Composed Attribute Properties")
	class ComposedAttributePropertiesTests {

		@Test
		@DisplayName("Composed attributes have no context")
		void composedHasNoContext() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeContext.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					1.5f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.of(AttributeContext.PART_COMPOSITE),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(base));
			sources.put("shape", List.of(multiplier));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertTrue(result.get(0).context().isEmpty(),
					"Composed attribute should have no context (already resolved)");
		}

		@Test
		@DisplayName("Composed attributes use addition operator")
		void composedUsesAddition() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					1.5f,
					MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(base));
			sources.put("shape", List.of(multiplier));

			List<Attribute> result = handler.compose(sources);

			assertEquals(AdditionOperator.getInstance(), result.get(0).operator(),
					"Composed attribute should use addition operator");
		}
	}
}
