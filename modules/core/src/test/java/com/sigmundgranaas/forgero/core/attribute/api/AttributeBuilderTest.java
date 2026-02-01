package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.DivisionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.SubtractionOperator;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttributeBuilder Fluent API")
class AttributeBuilderTest extends ForgeroTest {

	private static final OpenIdentifier DURABILITY = OpenIdentifier.of("forgero", "durability");
	private static final OpenIdentifier ATTACK_DAMAGE = OpenIdentifier.of("forgero", "attack_damage");
	private static final OpenIdentifier MINING_SPEED = OpenIdentifier.of("forgero", "mining_speed");

	/**
	 * Creates a simple test condition that always passes.
	 * Used for testing condition assignment without complex dependencies.
	 */
	private static Condition testCondition() {
		return Condition.ALWAYS_TRUE;
	}

	/**
	 * Creates a static condition for testing purposes.
	 */
	private static StaticCondition testStaticCondition() {
		return new StaticCondition() {
			@Override
			public boolean test(com.sigmundgranaas.forgero.core.property.context.ResolutionContext context) {
				return true;
			}

			@Override
			public OpenIdentifier type() {
				return OpenIdentifier.of("forgero", "test_condition");
			}
		};
	}

	@Nested
	@DisplayName("Factory Methods")
	class FactoryMethodTests {

		@Test
		@DisplayName("Attribute.of() creates builder with type and value")
		void attributeOfCreatesBuilderWithTypeAndValue() {
			Attribute attribute = Attribute.of(DURABILITY, 240f).build();

			assertEquals(DURABILITY, attribute.type());
			assertEquals(240f, attribute.value(), 0.001f);
		}

		@Test
		@DisplayName("Attribute.simple() creates simple attribute directly")
		void attributeSimpleCreatesSimpleAttribute() {
			Attribute attribute = Attribute.simple(DURABILITY, 100f);

			assertEquals(DURABILITY, attribute.type());
			assertEquals(100f, attribute.value(), 0.001f);
			assertTrue(attribute.scope().isEmpty());
			assertTrue(attribute.condition().isEmpty());
			assertInstanceOf(AdditionOperator.class, attribute.operator());
		}
	}

	@Nested
	@DisplayName("Basic Builder")
	class BasicBuilderTests {

		@Test
		@DisplayName("build() creates attribute with just type and value")
		void buildCreatesBasicAttribute() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f).build();

			assertEquals(ATTACK_DAMAGE, attribute.type());
			assertEquals(5f, attribute.value(), 0.001f);
		}

		@Test
		@DisplayName("build() uses defaults when no methods called")
		void buildUsesDefaultValues() {
			Attribute attribute = Attribute.of(MINING_SPEED, 3f).build();

			assertTrue(attribute.id().isEmpty(), "id should be empty by default");
			assertTrue(attribute.scope().isEmpty(), "scope should be empty by default");
			assertTrue(attribute.condition().isEmpty(), "condition should be empty by default");
			assertInstanceOf(AdditionOperator.class, attribute.operator(), "default operator should be addition");
			assertEquals(0, attribute.group(), "default group should be 0");
		}

		@Test
		@DisplayName("build() returns SimpleAttribute instance")
		void buildReturnsSimpleAttribute() {
			Attribute attribute = Attribute.of(DURABILITY, 240f).build();

			assertInstanceOf(SimpleAttribute.class, attribute);
		}
	}

	@Nested
	@DisplayName("Scope Methods")
	class ScopeMethodTests {

		@Test
		@DisplayName("forPartComposition() sets PART_COMPOSITE scope")
		void forPartCompositionSetsPartCompositeScope() {
			Attribute attribute = Attribute.of(DURABILITY, 240f)
					.forPartComposition()
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(AttributeScope.PART_COMPOSITE, attribute.scope().get());
		}

		@Test
		@DisplayName("forEquipmentComposition() sets EQUIPMENT_COMPOSITE scope")
		void forEquipmentCompositionSetsEquipmentCompositeScope() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.forEquipmentComposition()
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(AttributeScope.EQUIPMENT_COMPOSITE, attribute.scope().get());
		}

		@Test
		@DisplayName("forUpgradeSlots() sets UPGRADE scope")
		void forUpgradeSlotsSetsUpgradeScope() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.forUpgradeSlots()
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(AttributeScope.UPGRADE, attribute.scope().get());
		}

		@Test
		@DisplayName("localOnly() sets LOCAL scope")
		void localOnlySetsLocalScope() {
			Attribute attribute = Attribute.of(MINING_SPEED, 1.5f)
					.localOnly()
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(AttributeScope.LOCAL, attribute.scope().get());
		}

		@Test
		@DisplayName("withScope() sets custom scope")
		void withScopeSetCustomScope() {
			OpenIdentifier customScope = OpenIdentifier.of("forgero", "custom_scope");
			Attribute attribute = Attribute.of(DURABILITY, 50f)
					.withScope(customScope)
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(customScope, attribute.scope().get());
		}

		@Test
		@DisplayName("withScope(null) clears scope")
		void withScopeNullClearsScope() {
			Attribute attribute = Attribute.of(DURABILITY, 50f)
					.forPartComposition()
					.withScope(null)
					.build();

			assertTrue(attribute.scope().isEmpty());
		}

		@Test
		@DisplayName("withoutScope() clears previously set scope")
		void withoutScopeClearsPreviousScope() {
			Attribute attribute = Attribute.of(DURABILITY, 240f)
					.forPartComposition()
					.withoutScope()
					.build();

			assertTrue(attribute.scope().isEmpty());
		}

		@Test
		@DisplayName("scope methods can be chained and last one wins")
		void scopeMethodsCanBeChainedAndLastWins() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.forPartComposition()
					.forUpgradeSlots()
					.localOnly()
					.build();

			assertTrue(attribute.scope().isPresent());
			assertEquals(AttributeScope.LOCAL, attribute.scope().get());
		}
	}

	@Nested
	@DisplayName("Operator Methods")
	class OperatorMethodTests {

		@Test
		@DisplayName("add() sets AdditionOperator")
		void addSetsAdditionOperator() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.add()
					.build();

			assertInstanceOf(AdditionOperator.class, attribute.operator());
		}

		@Test
		@DisplayName("multiply() sets MultiplicationOperator")
		void multiplySetsMultiplicationOperator() {
			Attribute attribute = Attribute.of(DURABILITY, 1.5f)
					.multiply()
					.build();

			assertInstanceOf(MultiplicationOperator.class, attribute.operator());
		}

		@Test
		@DisplayName("subtract() sets SubtractionOperator")
		void subtractSetsSubtractionOperator() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 2f)
					.subtract()
					.build();

			assertInstanceOf(SubtractionOperator.class, attribute.operator());
		}

		@Test
		@DisplayName("divide() sets DivisionOperator")
		void divideSetsDeivisionOperator() {
			Attribute attribute = Attribute.of(MINING_SPEED, 0.5f)
					.divide()
					.build();

			assertInstanceOf(DivisionOperator.class, attribute.operator());
		}

		@Test
		@DisplayName("withOperator() sets custom operator")
		void withOperatorSetsCustomOperator() {
			Operator customOperator = MultiplicationOperator.getInstance();
			Attribute attribute = Attribute.of(DURABILITY, 2f)
					.withOperator(customOperator)
					.build();

			assertEquals(customOperator, attribute.operator());
		}

		@Test
		@DisplayName("operator methods can be chained and last one wins")
		void operatorMethodsCanBeChainedAndLastWins() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.add()
					.multiply()
					.subtract()
					.build();

			assertInstanceOf(SubtractionOperator.class, attribute.operator());
		}

		@Test
		@DisplayName("default operator is AdditionOperator")
		void defaultOperatorIsAddition() {
			Attribute attribute = Attribute.of(DURABILITY, 100f).build();

			assertInstanceOf(AdditionOperator.class, attribute.operator());
		}
	}

	@Nested
	@DisplayName("Condition Methods")
	class ConditionMethodTests {

		@Test
		@DisplayName("when() sets single condition")
		void whenSetsSingleCondition() {
			Condition condition = testCondition();
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.when(condition)
					.build();

			assertTrue(attribute.condition().isPresent());
			assertEquals(condition, attribute.condition().get());
		}

		@Test
		@DisplayName("when(null) clears condition")
		void whenNullClearsCondition() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.when(testCondition())
					.when(null)
					.build();

			assertTrue(attribute.condition().isEmpty());
		}

		@Test
		@DisplayName("whenAll() combines conditions with AND semantics")
		void whenAllCombinesConditionsWithAnd() {
			Condition condition = testCondition();
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.whenAll(condition, condition)
					.build();

			assertTrue(attribute.condition().isPresent());
			assertNotNull(attribute.condition().get());
		}

		@Test
		@DisplayName("whenAll() with static conditions")
		void whenAllWithStaticConditions() {
			StaticCondition condition1 = testStaticCondition();
			StaticCondition condition2 = testStaticCondition();

			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.whenAll(
							Condition.ofStatic(condition1),
							Condition.ofStatic(condition2)
					)
					.build();

			assertTrue(attribute.condition().isPresent());
			Condition result = attribute.condition().get();
			assertEquals(2, result.staticConditions().size());
		}

		@Test
		@DisplayName("whenAny() combines conditions with OR semantics")
		void whenAnyCombinesConditionsWithOr() {
			Condition condition = testCondition();
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.whenAny(condition, condition)
					.build();

			assertTrue(attribute.condition().isPresent());
			assertNotNull(attribute.condition().get());
		}

		@Test
		@DisplayName("always() clears previously set condition")
		void alwaysClearsPreviousCondition() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.when(testCondition())
					.always()
					.build();

			assertTrue(attribute.condition().isEmpty());
		}

		@Test
		@DisplayName("condition methods can be chained and last one wins")
		void conditionMethodsCanBeChainedAndLastWins() {
			Condition finalCondition = testCondition();
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.whenAll(testCondition(), testCondition())
					.when(finalCondition)
					.build();

			assertTrue(attribute.condition().isPresent());
			assertEquals(finalCondition, attribute.condition().get());
		}
	}

	@Nested
	@DisplayName("Configuration Methods")
	class ConfigurationMethodTests {

		@Test
		@DisplayName("inGroup() sets computation group")
		void inGroupSetsComputationGroup() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.inGroup(5)
					.build();

			assertEquals(5, attribute.group());
		}

		@Test
		@DisplayName("inGroup() default is 0")
		void inGroupDefaultIsZero() {
			Attribute attribute = Attribute.of(DURABILITY, 100f).build();

			assertEquals(0, attribute.group());
		}

		@Test
		@DisplayName("inGroup() accepts negative values")
		void inGroupAcceptsNegativeValues() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.inGroup(-1)
					.build();

			assertEquals(-1, attribute.group());
		}

		@Test
		@DisplayName("withId(OpenIdentifier) sets attribute id")
		void withIdOpenIdentifierSetsAttributeId() {
			OpenIdentifier id = OpenIdentifier.of("forgero", "custom_attribute");
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.withId(id)
					.build();

			assertTrue(attribute.id().isPresent());
			assertEquals(id, attribute.id().get());
		}

		@Test
		@DisplayName("withId(String) sets attribute id")
		void withIdStringSetsAttributeId() {
			String idString = "custom_attribute";
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.withId(idString)
					.build();

			assertTrue(attribute.id().isPresent());
			assertEquals(OpenIdentifier.of(idString), attribute.id().get());
		}

		@Test
		@DisplayName("withId(null) clears id")
		void withIdNullClearsId() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.withId("some_id")
					.withId((OpenIdentifier) null)
					.build();

			assertTrue(attribute.id().isEmpty());
		}

		@Test
		@DisplayName("withId(String null) clears id")
		void withIdStringNullClearsId() {
			Attribute attribute = Attribute.of(DURABILITY, 100f)
					.withId("some_id")
					.withId((String) null)
					.build();

			assertTrue(attribute.id().isEmpty());
		}
	}

	@Nested
	@DisplayName("Method Chaining")
	class MethodChainingTests {

		@Test
		@DisplayName("all methods can be chained together")
		void allMethodsCanBeChainedTogether() {
			Attribute attribute = Attribute.of(ATTACK_DAMAGE, 5f)
					.forUpgradeSlots()
					.multiply()
					.when(testCondition())
					.inGroup(2)
					.withId("sneak_bonus")
					.build();

			assertEquals(ATTACK_DAMAGE, attribute.type());
			assertEquals(5f, attribute.value(), 0.001f);
			assertEquals(AttributeScope.UPGRADE, attribute.scope().get());
			assertInstanceOf(MultiplicationOperator.class, attribute.operator());
			assertTrue(attribute.condition().isPresent());
			assertEquals(2, attribute.group());
			assertEquals(OpenIdentifier.of("sneak_bonus"), attribute.id().get());
		}

		@Test
		@DisplayName("builder pattern returns this for chaining")
		void builderPatternReturnsThisForChaining() {
			AttributeBuilder builder = Attribute.of(DURABILITY, 240f);

			assertSame(builder, builder.forPartComposition());
			assertSame(builder, builder.multiply());
			assertSame(builder, builder.inGroup(1));
			assertSame(builder, builder.withId("test_id"));
			assertSame(builder, builder.when(testCondition()));
		}
	}

	@Nested
	@DisplayName("Real-World Usage Examples")
	class RealWorldExamplesTests {

		@Test
		@DisplayName("material base value for part composition")
		void materialBaseValueForPartComposition() {
			Attribute ironDurability = Attribute.of(DURABILITY, 240f)
					.forPartComposition()
					.build();

			assertEquals(DURABILITY, ironDurability.type());
			assertEquals(240f, ironDurability.value(), 0.001f);
			assertEquals(AttributeScope.PART_COMPOSITE, ironDurability.scope().get());
			assertInstanceOf(AdditionOperator.class, ironDurability.operator());
		}

		@Test
		@DisplayName("shape multiplier for part composition")
		void shapeMultiplierForPartComposition() {
			Attribute pickaxeMultiplier = Attribute.of(DURABILITY, 1.0f)
					.multiply()
					.forPartComposition()
					.build();

			assertEquals(DURABILITY, pickaxeMultiplier.type());
			assertEquals(1.0f, pickaxeMultiplier.value(), 0.001f);
			assertEquals(AttributeScope.PART_COMPOSITE, pickaxeMultiplier.scope().get());
			assertInstanceOf(MultiplicationOperator.class, pickaxeMultiplier.operator());
		}

		@Test
		@DisplayName("upgrade-only bonus with condition")
		void upgradeOnlyBonusWithCondition() {
			Condition sneakCondition = testCondition();
			Attribute sneakBonus = Attribute.of(ATTACK_DAMAGE, 5f)
					.forUpgradeSlots()
					.when(sneakCondition)
					.build();

			assertEquals(ATTACK_DAMAGE, sneakBonus.type());
			assertEquals(5f, sneakBonus.value(), 0.001f);
			assertEquals(AttributeScope.UPGRADE, sneakBonus.scope().get());
			assertTrue(sneakBonus.condition().isPresent());
		}

		@Test
		@DisplayName("local attribute that doesn't propagate")
		void localAttributeThatDoesNotPropagate() {
			OpenIdentifier internalModifier = OpenIdentifier.of("forgero", "internal_modifier");
			Attribute internalValue = Attribute.of(internalModifier, 1.5f)
					.localOnly()
					.build();

			assertEquals(internalModifier, internalValue.type());
			assertEquals(1.5f, internalValue.value(), 0.001f);
			assertEquals(AttributeScope.LOCAL, internalValue.scope().get());
		}

		@Test
		@DisplayName("equipment composition with multiple groups")
		void equipmentCompositionWithMultipleGroups() {
			// Base value (group 0, computed first)
			Attribute baseValue = Attribute.of(DURABILITY, 100f)
					.forEquipmentComposition()
					.inGroup(0)
					.build();

			// Multiplier (group 1, computed after base)
			Attribute multiplier = Attribute.of(DURABILITY, 1.2f)
					.multiply()
					.forEquipmentComposition()
					.inGroup(1)
					.build();

			assertEquals(0, baseValue.group());
			assertEquals(1, multiplier.group());
			assertEquals(AttributeScope.EQUIPMENT_COMPOSITE, baseValue.scope().get());
			assertEquals(AttributeScope.EQUIPMENT_COMPOSITE, multiplier.scope().get());
		}
	}
}
