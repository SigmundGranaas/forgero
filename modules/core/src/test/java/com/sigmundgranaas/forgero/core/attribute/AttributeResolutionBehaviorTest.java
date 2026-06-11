package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.testutils.TestIdentifiers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavioral tests for the attribute resolution system.
 *
 * These tests verify the core behaviors of attribute resolution through the public Resolver API:
 * - Attributes aggregate from parts to tools (additive composition)
 * - Conditions filter attributes appropriately
 * - Components maintain isolation (no state leakage)
 *
 * All tests use the public API (Resolver, Component, AttributeQueryResult) and never
 * access implementation classes directly. This ensures tests remain valid even when
 * internal implementations change.
 */
@DisplayName("Attribute Resolution Behavior")
class AttributeResolutionBehaviorTest extends ForgeroTest {

	private static final String HEAD_TAG = "head";
	private static final String HANDLE_TAG = "handle";

	@Nested
	@DisplayName("Condition Filtering")
	class ConditionFilteringTests {

		/**
		 * Tests that attributes with conditions that return FALSE are not included.
		 * This is the core mechanism that prevents armor from leaking to tools.
		 */
		@Test
		@DisplayName("Attributes with false conditions should be excluded")
		void attributesWithFalseConditionsShouldBeExcluded() {
			// Condition that always returns false
			StaticCondition alwaysFalse = new StaticCondition() {
				@Override
				public boolean test(ResolutionContext ctx) {
					return false;
				}

				@Override
				public OpenIdentifier type() {
					return TestIdentifiers.id("test:always_false");
				}
			};

			Condition falseCondition = new Condition(List.of(alwaysFalse), List.of());

			// Create a part with both conditioned and unconditioned attributes
			Component part = part("test_part")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 100f)  // Unconditional - should apply
					.withConditionalAttribute(DefaultAttributes.ARMOR, 5f, falseCondition)  // Conditioned - should NOT apply
					.build();

			Component tool = tool("test_tool")
					.withTag("tool")
					.withPart(part, "slot", TestIdentifiers.id(HEAD_TAG))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);

			// Unconditioned attribute should apply
			assertEquals(100f, result.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Unconditioned durability should be included");

			// Conditioned attribute with false condition should NOT apply
			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR),
					"Attribute with false condition should be excluded");
		}

		/**
		 * Tests that attributes with conditions that return TRUE are included.
		 */
		@Test
		@DisplayName("Attributes with true conditions should be included")
		void attributesWithTrueConditionsShouldBeIncluded() {
			// Condition that always returns true
			StaticCondition alwaysTrue = new StaticCondition() {
				@Override
				public boolean test(ResolutionContext ctx) {
					return true;
				}

				@Override
				public OpenIdentifier type() {
					return TestIdentifiers.id("test:always_true");
				}
			};

			Condition trueCondition = new Condition(List.of(alwaysTrue), List.of());

			Component part = part("test_part")
					.withTag(HEAD_TAG)
					.withConditionalAttribute(DefaultAttributes.ARMOR, 5f, trueCondition)
					.build();

			Component tool = tool("test_tool")
					.withTag("tool")
					.withPart(part, "slot", TestIdentifiers.id(HEAD_TAG))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);

			// Conditioned attribute with true condition SHOULD apply
			assertEquals(5f, result.getValue(DefaultAttributes.ARMOR), 0.1f,
					"Attribute with true condition should be included");
		}

		/**
		 * Tests that ALWAYS_TRUE condition (empty conditions list) allows attributes through.
		 */
		@Test
		@DisplayName("Attributes with ALWAYS_TRUE condition should be included")
		void attributesWithAlwaysTrueConditionShouldBeIncluded() {
			Component part = part("test_part")
					.withTag(HEAD_TAG)
					.withConditionalAttribute(DefaultAttributes.DURABILITY, 100f, Condition.ALWAYS_TRUE)
					.build();

			Component tool = tool("test_tool")
					.withTag("tool")
					.withPart(part, "slot", TestIdentifiers.id(HEAD_TAG))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);

			assertEquals(100f, result.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Attribute with ALWAYS_TRUE condition should be included");
		}
	}

	@Nested
	@DisplayName("Attribute Aggregation")
	class AttributeAggregationTests {

		/**
		 * Tests that attributes from multiple parts are summed correctly.
		 */
		@Test
		@DisplayName("Attributes from multiple parts should aggregate")
		void attributesFromMultiplePartsShouldAggregate() {
			Component head = part("head")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 100f)
					.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)
					.build();

			Component handle = part("handle")
					.withTag(HANDLE_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 25f)
					.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1f)
					.build();

			Component tool = tool("test_tool")
					.withTag("tool")
					.withPart(head, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.withPart(handle, "handle_slot", TestIdentifiers.id(HANDLE_TAG))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);

			assertEquals(125f, result.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Durability should sum: 100 + 25 = 125");
			assertEquals(6f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.1f,
					"Attack damage should sum: 5 + 1 = 6");
		}

		/**
		 * Tests that attributes from a part without a specific attribute don't affect the total.
		 */
		@Test
		@DisplayName("Missing attributes should not affect aggregation")
		void missingAttributesShouldNotAffectAggregation() {
			Component head = part("head")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 100f)
					.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
					.build();

			// Handle only has durability, no mining speed
			Component handle = part("handle")
					.withTag(HANDLE_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 25f)
					.build();

			Component tool = tool("test_tool")
					.withTag("tool")
					.withPart(head, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.withPart(handle, "handle_slot", TestIdentifiers.id(HANDLE_TAG))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);

			assertEquals(125f, result.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Durability should sum normally");
			assertEquals(6f, result.getValue(DefaultAttributes.MINING_SPEED), 0.1f,
					"Mining speed should only come from head");
		}
	}

	@Nested
	@DisplayName("Component Isolation")
	class ComponentIsolationTests {

		/**
		 * Tests that separate tool instances don't share attribute state.
		 */
		@Test
		@DisplayName("Separate tools should have independent attributes")
		void separateToolsShouldHaveIndependentAttributes() {
			Component weakHead = part("weak_head")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 50f)
					.build();

			Component strongHead = part("strong_head")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 500f)
					.build();

			Component handle = part("handle")
					.withTag(HANDLE_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 25f)
					.build();

			Component weakTool = tool("weak_tool")
					.withTag("tool")
					.withPart(weakHead, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.withPart(handle, "handle_slot", TestIdentifiers.id(HANDLE_TAG))
					.build();

			Component strongTool = tool("strong_tool")
					.withTag("tool")
					.withPart(strongHead, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.withPart(handle, "handle_slot", TestIdentifiers.id(HANDLE_TAG))
					.build();

			AttributeQueryResult weakResult = resolveAttributes(weakTool);
			AttributeQueryResult strongResult = resolveAttributes(strongTool);

			assertEquals(75f, weakResult.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Weak tool should have 50 + 25 = 75 durability");
			assertEquals(525f, strongResult.getValue(DefaultAttributes.DURABILITY), 0.1f,
					"Strong tool should have 500 + 25 = 525 durability");

			// Ensure they're truly independent
			assertNotEquals(
					weakResult.getValue(DefaultAttributes.DURABILITY),
					strongResult.getValue(DefaultAttributes.DURABILITY),
					"Tools should have different durability values");
		}

		/**
		 * Tests that modifying one component doesn't affect another.
		 */
		@Test
		@DisplayName("Components should be immutable and isolated")
		void componentsShouldBeImmutableAndIsolated() {
			Component head = part("head")
					.withTag(HEAD_TAG)
					.withAttribute(DefaultAttributes.DURABILITY, 100f)
					.build();

			Component tool1 = tool("tool1")
					.withTag("tool")
					.withPart(head, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.build();

			Component tool2 = tool("tool2")
					.withTag("tool")
					.withPart(head, "head_slot", TestIdentifiers.id(HEAD_TAG))
					.build();

			AttributeQueryResult result1 = resolveAttributes(tool1);
			AttributeQueryResult result2 = resolveAttributes(tool2);

			// Both should have same value since they share the same head
			assertEquals(result1.getValue(DefaultAttributes.DURABILITY),
					result2.getValue(DefaultAttributes.DURABILITY),
					"Tools sharing same parts should have same attributes");
		}
	}
}
