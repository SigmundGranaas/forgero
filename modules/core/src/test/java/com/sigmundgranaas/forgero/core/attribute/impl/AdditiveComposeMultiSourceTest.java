package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the additiveComposeMultiSource() fallback path in CompositeAttributeBakingStrategy.
 *
 * <p>This fallback is triggered when:</p>
 * <ol>
 *   <li>Multiple sources exist (>= 2)</li>
 *   <li>Intersection-based composition (PartCompositeScopeHandler) returns empty</li>
 * </ol>
 *
 * <p>The fallback behavior:</p>
 * <ul>
 *   <li>Sums all attribute values by type</li>
 *   <li>Includes attribute if: no scope (resolved) OR scope from 2+ different sources</li>
 * </ul>
 */
@DisplayName("AdditiveComposeMultiSource Fallback Tests")
class AdditiveComposeMultiSourceTest {

	private CompositeAttributeBakingStrategy strategy;

	@BeforeEach
	void setUp() {
		strategy = new CompositeAttributeBakingStrategy();
	}

	@Nested
	@DisplayName("Fallback Trigger Conditions")
	class FallbackTriggerTests {

		@Test
		@DisplayName("Fallback triggers when intersection composition returns empty")
		void fallbackTriggersWhenIntersectionEmpty() {
			// Create two sources with only additive attributes (no multipliers)
			// This should fail intersection (needs base + multiplier) and trigger fallback
			SimpleAttribute attr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					5f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute attr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			// Create a structured component with two parts
			Component part1 = createSimpleComponent("test:part1", List.of(attr1));
			Component part2 = createSimpleComponent("test:part2", List.of(attr2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			// Fallback should have summed the values: 5 + 3 = 8
			// Attribute has scope and comes from 2 sources, so it should be included
			assertEquals(1, result.size());
			assertEquals(8f, result.get(0).value(), 0.001f);
		}
	}

	@Nested
	@DisplayName("Additive Summing Behavior")
	class AdditiveSummingTests {

		@Test
		@DisplayName("Sums attributes by type across multiple sources")
		void sumsAttributesByType() {
			// Two sources with same type, different values
			SimpleAttribute attr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute attr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			Component part1 = createSimpleComponent("test:part1", List.of(attr1));
			Component part2 = createSimpleComponent("test:part2", List.of(attr2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			assertEquals(1, result.size());
			assertEquals(150f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Keeps different attribute types separate")
		void keepsDifferentTypesSeparate() {
			SimpleAttribute durability1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute attack1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					5f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute durability2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute attack2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			Component part1 = createSimpleComponent("test:part1", List.of(durability1, attack1));
			Component part2 = createSimpleComponent("test:part2", List.of(durability2, attack2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			assertEquals(2, result.size());

			Map<OpenIdentifier, Float> resultMap = new HashMap<>();
			for (Attribute attr : result) {
				resultMap.put(attr.type(), attr.value());
			}

			assertEquals(150f, resultMap.get(DefaultAttributes.DURABILITY), 0.001f);
			assertEquals(8f, resultMap.get(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}
	}

	@Nested
	@DisplayName("Source Requirement Tests")
	class SourceRequirementTests {

		@Test
		@DisplayName("Includes scoped attributes only if from 2+ sources")
		void requiresTwoSourcesForScopedAttributes() {
			// Only one source has this attribute - should be excluded
			SimpleAttribute singleSourceAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					6f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			// Both sources have this attribute - should be included
			SimpleAttribute multiSourceAttr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute multiSourceAttr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			Component part1 = createSimpleComponent("test:part1", List.of(singleSourceAttr, multiSourceAttr1));
			Component part2 = createSimpleComponent("test:part2", List.of(multiSourceAttr2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			// Only durability should be included (2 sources), mining_speed excluded (1 source)
			assertEquals(1, result.size());
			assertEquals(DefaultAttributes.DURABILITY, result.get(0).type());
			assertEquals(150f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Includes no-scope attributes regardless of source count")
		void includesNoScopeAttributesAlways() {
			// No scope (resolved) attribute from single source - should be included
			SimpleAttribute noScopeAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					6f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(), // No scope
					Optional.empty()
			);
			// Scoped attribute from both sources
			SimpleAttribute scopedAttr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			SimpleAttribute scopedAttr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			Component part1 = createSimpleComponent("test:part1", List.of(noScopeAttr, scopedAttr1));
			Component part2 = createSimpleComponent("test:part2", List.of(scopedAttr2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			// Both should be included: mining_speed (no scope) and durability (2 sources)
			assertEquals(2, result.size());

			Map<OpenIdentifier, Float> resultMap = new HashMap<>();
			for (Attribute attr : result) {
				resultMap.put(attr.type(), attr.value());
			}

			assertTrue(resultMap.containsKey(DefaultAttributes.MINING_SPEED));
			assertTrue(resultMap.containsKey(DefaultAttributes.DURABILITY));
		}
	}

	@Nested
	@DisplayName("Mixed Resolved and Scoped Attributes")
	class MixedAttributeTests {

		@Test
		@DisplayName("Resolved attrs pass through separately from scoped composition")
		void resolvedAttrsPassThroughSeparately() {
			// Resolved (no scope) attribute - goes through collectDefaultAttributes
			SimpleAttribute resolvedAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					2f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			// Scoped attribute of same type - goes through composition
			// Since it's from only one source, it gets discarded by composition rules
			SimpleAttribute scopedAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			Component part1 = createSimpleComponent("test:part1", List.of(resolvedAttr));
			Component part2 = createSimpleComponent("test:part2", List.of(scopedAttr));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			// Resolved attr (2.0) passes through via collectDefaultAttributes
			// Scoped attr (3.0) is discarded: composition path sees only 1 source for it
			// Result: only the resolved attr remains
			assertEquals(1, result.size());
			assertEquals(2f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Scoped attrs from 2+ sources get summed even with resolved attrs present")
		void scopedAttrsFromMultipleSourcesSummed() {
			// Resolved (no scope) attribute
			SimpleAttribute resolvedAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					2f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			// Scoped attribute from part1
			SimpleAttribute scopedAttr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);
			// Scoped attribute from part2
			SimpleAttribute scopedAttr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeScope.PART_COMPOSITE),
					Optional.empty()
			);

			// part1 has both resolved and scoped attrs
			Component part1 = createSimpleComponent("test:part1", List.of(resolvedAttr, scopedAttr1));
			Component part2 = createSimpleComponent("test:part2", List.of(scopedAttr2));
			StructuredComponent structured = createStructuredComponent("test:root", part1, part2);

			List<Attribute> result = strategy.bake(Stream.of(structured));

			// Scoped attrs (3 + 4 = 7) come from 2 sources, so additiveComposeMultiSource includes them
			// Resolved attr (2) passes through collectDefaultAttributes separately
			// These are SEPARATE results (same type, but different paths)
			assertEquals(2, result.size());

			// Sum all values for ATTACK_DAMAGE type
			float totalDamage = result.stream()
					.filter(a -> a.type().equals(DefaultAttributes.ATTACK_DAMAGE))
					.map(Attribute::value)
					.reduce(0f, Float::sum);
			assertEquals(9f, totalDamage, 0.001f);
		}
	}

	// =====================================================================
	// Helper methods for creating test components
	// =====================================================================

	private Component createSimpleComponent(String id, List<Attribute> attributes) {
		return new TestComponent(OpenIdentifier.parse(id), attributes);
	}

	private StructuredComponent createStructuredComponent(String id, Component part1, Component part2) {
		return new TestStructuredComponent(OpenIdentifier.parse(id), part1, part2);
	}

	/**
	 * Simple test component that holds attributes.
	 */
	private static class TestComponent implements Component {
		private final OpenIdentifier id;
		private final List<Attribute> attributes;

		TestComponent(OpenIdentifier id, List<Attribute> attributes) {
			this.id = id;
			this.attributes = new ArrayList<>(attributes);
		}

		@Override
		public OpenIdentifier id() {
			return id;
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return Set.of();
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("test:material");
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Map.of(Attribute.KEY.key(), new ArrayList<>(attributes));
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this; // Not needed for tests
		}
	}

	/**
	 * Test structured component with two parts.
	 */
	private static class TestStructuredComponent extends TestComponent implements StructuredComponent {
		private final ComponentStructure structure;

		TestStructuredComponent(OpenIdentifier id, Component part1, Component part2) {
			super(id, List.of());
			List<ComponentPart> parts = List.of(
					new ComponentPart(
							new OpenIdentifier("test", "slot1"),
							new OpenIdentifier("test", "part_type"),
							"Part 1",
							SlotValidator.ACCEPT_ALL,
							part1
					),
					new ComponentPart(
							new OpenIdentifier("test", "slot2"),
							new OpenIdentifier("test", "part_type"),
							"Part 2",
							SlotValidator.ACCEPT_ALL,
							part2
					)
			);
			this.structure = ComponentStructure.of(parts);
		}

		@Override
		public ComponentStructure structure() {
			return structure;
		}

		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return this; // Not needed for tests
		}
	}
}
