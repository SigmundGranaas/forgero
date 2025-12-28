package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.impl.SlotManagerImpl;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SlotManager with property resolution.
 * Tests that slot-based conditional properties work correctly with the slot management API.
 */
class SlotManagerIntegrationTest {

	private static final OpenIdentifier OFFENSIVE_GEM_SLOT_TYPE = OpenIdentifier.of("forgero:offensive_gem_slot");
	private static final OpenIdentifier UTILITY_GEM_SLOT_TYPE = OpenIdentifier.of("forgero:utility_gem_slot");
	private static final OpenIdentifier GEM_TYPE = OpenIdentifier.of("forgero:gem");

	private SlotManager slotManager;
	private ComponentMutater mutater;

	@BeforeEach
	void setUp() {
		mutater = new ComponentMutaterImpl();
		slotManager = new SlotManagerImpl(mutater);
	}

	// ============================================================
	// InSlotTypeCondition Integration Tests
	// ============================================================

	@Nested
	@DisplayName("InSlotTypeCondition Integration")
	class InSlotTypeConditionTests {

		@Test
		@DisplayName("upgrade with in_slot_type condition should only apply in matching slot")
		void upgradeWithInSlotTypeCondition_OnlyAppliesInMatchingSlot() {
			// Create tool with two gem slots: offensive and utility
			TestComponent tool = createToolWithTwoGemSlots();

			// Create gem upgrade with attack_damage that only applies in offensive slots
			TestComponent offensiveGem = createOffensiveGem();

			// Install in offensive slot
			InstallationResult offensiveResult = slotManager.install(tool, offensiveGem);

			assertTrue(offensiveResult.success(), "Should install successfully");
			Component toolWithOffensiveGem = offensiveResult.component().orElseThrow();

			// Verify the gem was installed in offensive slot
			List<ComponentUpgradeSlot> filledSlots = slotManager.getFilledComponentUpgradeSlots(toolWithOffensiveGem);
			assertEquals(1, filledSlots.size(), "Should have one filled slot");

			ComponentUpgradeSlot filledSlot = filledSlots.get(0);
			assertEquals(OFFENSIVE_GEM_SLOT_TYPE, filledSlot.type(),
				"Gem should be in offensive slot");

			// Verify the gem has the conditional property
			Component installedGem = filledSlot.content().orElseThrow();
			assertTrue(installedGem instanceof TestComponent,
				"Installed component should be our test gem");

			TestComponent gem = (TestComponent) installedGem;
			assertEquals(1, gem.propertiesAsMap().size(),
				"Gem should have properties");

			List<?> attributes = gem.propertiesAsMap().get("attributes");
			assertNotNull(attributes, "Gem should have attributes");
			assertEquals(1, attributes.size(), "Gem should have one attribute");

			Object attr = attributes.get(0);
			assertTrue(attr instanceof SimpleAttribute,
				"Attribute should be SimpleAttribute");

			SimpleAttribute simpleAttr = (SimpleAttribute) attr;
			assertEquals("forgero:attack_damage", simpleAttr.type().toString(),
				"Attribute should be attack_damage");
			assertEquals(5.0f, simpleAttr.value(), 0.001f, "Attribute value should be 5");

			// Verify the attribute has InSlotTypeCondition
			Optional<Condition> conditionOpt = simpleAttr.condition();
			assertTrue(conditionOpt.isPresent(), "Attribute should have condition");

			Condition condition = conditionOpt.get();
			assertFalse(condition.staticConditions().isEmpty(),
				"Condition should have static conditions");
			assertEquals(1, condition.staticConditions().size(),
				"Should have one static condition");

			assertTrue(condition.staticConditions().get(0) instanceof InSlotTypeCondition,
				"Static condition should be InSlotTypeCondition");

			InSlotTypeCondition slotCondition = (InSlotTypeCondition) condition.staticConditions().get(0);
			assertEquals(OFFENSIVE_GEM_SLOT_TYPE, slotCondition.slotType(),
				"Condition should require offensive slot");
		}

		@Test
		@DisplayName("multiple upgrades with different slot type conditions should coexist")
		void multipleUpgradesWithDifferentConditions_ShouldCoexist() {
			// Create tool with two gem slots
			TestComponent tool = createToolWithTwoGemSlots();

			// Create two gems: one for offensive, one for utility
			TestComponent offensiveGem = createOffensiveGem();
			TestComponent utilityGem = createUtilityGem();

			// Install offensive gem
			InstallationResult result1 = slotManager.install(tool, offensiveGem);
			assertTrue(result1.success(), "Should install offensive gem");

			// Install utility gem
			InstallationResult result2 = slotManager.install(
				result1.component().orElseThrow(), utilityGem);
			assertTrue(result2.success(), "Should install utility gem");

			Component toolWithBothGems = result2.component().orElseThrow();

			// Verify both gems are installed
			List<ComponentUpgradeSlot> filledSlots = slotManager.getFilledComponentUpgradeSlots(toolWithBothGems);
			assertEquals(2, filledSlots.size(), "Should have two filled slots");

			// Verify slot types
			Set<OpenIdentifier> slotTypes = Set.of(
				filledSlots.get(0).type(),
				filledSlots.get(1).type()
			);
			assertTrue(slotTypes.contains(OFFENSIVE_GEM_SLOT_TYPE),
				"Should have offensive slot filled");
			assertTrue(slotTypes.contains(UTILITY_GEM_SLOT_TYPE),
				"Should have utility slot filled");
		}

		@Test
		@DisplayName("removing upgrade should preserve other slot contents")
		void removingUpgrade_PreservesOtherSlots() {
			// Create tool with two gem slots
			TestComponent tool = createToolWithTwoGemSlots();

			// Install two gems
			TestComponent offensiveGem = createOffensiveGem();
			TestComponent utilityGem = createUtilityGem();

			Component toolWithOffensive = slotManager.install(tool, offensiveGem)
				.component().orElseThrow();
			Component toolWithBoth = slotManager.install(toolWithOffensive, utilityGem)
				.component().orElseThrow();

			// Remove offensive gem
			Component toolWithUtilityOnly = slotManager.removeUpgrade(
				toolWithBoth, offensiveGem.id());

			// Verify only utility gem remains
			List<ComponentUpgradeSlot> filledSlots = slotManager.getFilledComponentUpgradeSlots(toolWithUtilityOnly);
			assertEquals(1, filledSlots.size(), "Should have one filled slot");
			assertEquals(UTILITY_GEM_SLOT_TYPE, filledSlots.get(0).type(),
				"Remaining gem should be in utility slot");
		}
	}

	// ============================================================
	// Slot Validation Integration Tests
	// ============================================================

	@Nested
	@DisplayName("Slot Validation Integration")
	class SlotValidationTests {

		@Test
		@DisplayName("installing upgrade without required tag should fail")
		void installingUpgradeWithoutRequiredTag_ShouldFail() {
			// Create tool with gem slot that requires gem tag
			TestComponent tool = createToolWithStrictGemSlot();

			// Create upgrade without gem tag
			TestComponent nonGem = new TestComponent(
				OpenIdentifier.of("forgero:not_a_gem"),
				Set.of(OpenIdentifier.of("forgero:material")), // No gem tag
				Map.of(),
				ComponentUpgrades.empty()
			);

			// Attempt installation
			InstallationResult result = slotManager.install(tool, nonGem);

			assertFalse(result.success(), "Installation should fail");
			assertTrue(result.errorMessage().isPresent(), "Should have error message");
			assertTrue(result.errorMessage().get().contains("No compatible slot"),
				"Error should mention no compatible slot");
		}

		@Test
		@DisplayName("installing upgrade with required tag should succeed")
		void installingUpgradeWithRequiredTag_ShouldSucceed() {
			// Create tool with gem slot that requires gem tag
			TestComponent tool = createToolWithStrictGemSlot();

			// Create upgrade with gem tag
			TestComponent gem = new TestComponent(
				OpenIdentifier.of("forgero:ruby"),
				Set.of(GEM_TYPE), // Has gem tag
				Map.of("attributes", List.of(
					new SimpleAttribute(
						OpenIdentifier.of("forgero:attack_damage"),
						5.0f
					)
				)),
				ComponentUpgrades.empty()
			);

			// Attempt installation
			InstallationResult result = slotManager.install(tool, gem);

			assertTrue(result.success(), "Installation should succeed");
			assertTrue(result.component().isPresent(), "Should have resulting component");
		}
	}

	// ============================================================
	// Sequential Installation Tests
	// ============================================================

	@Nested
	@DisplayName("Sequential Installation")
	class SequentialInstallationTests {

		@Test
		@DisplayName("sequential installations should fill slots in order")
		void sequentialInstallations_FillSlotsInOrder() {
			// Create tool with 3 gem slots
			TestComponent tool = createToolWithThreeGemSlots();

			// Create three gems
			TestComponent gem1 = createGem("ruby", 5.0);
			TestComponent gem2 = createGem("emerald", 3.0);
			TestComponent gem3 = createGem("sapphire", 4.0);

			// Install sequentially
			Component step1 = slotManager.install(tool, gem1).component().orElseThrow();
			assertEquals(1, slotManager.countFilledSlots(step1),
				"Should have 1 filled slot");

			Component step2 = slotManager.install(step1, gem2).component().orElseThrow();
			assertEquals(2, slotManager.countFilledSlots(step2),
				"Should have 2 filled slots");

			Component step3 = slotManager.install(step2, gem3).component().orElseThrow();
			assertEquals(3, slotManager.countFilledSlots(step3),
				"Should have 3 filled slots");

			// Verify all slots filled
			assertTrue(slotManager.areAllSlotsFilled(step3),
				"All slots should be filled");
		}

		@Test
		@DisplayName("installing when all slots full should fail")
		void installingWhenAllSlotsFull_ShouldFail() {
			// Create tool with 1 gem slot
			TestComponent tool = new TestComponent(
				OpenIdentifier.of("forgero:sword"),
				Set.of(),
				Map.of(),
				ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(
						OpenIdentifier.of("gem_slot"),
						GEM_TYPE,
						"Gem Slot"
					)
				))
			);

			// Fill the slot
			TestComponent gem1 = createGem("ruby", 5.0);
			Component fullTool = slotManager.install(tool, gem1).component().orElseThrow();

			// Attempt to install another gem
			TestComponent gem2 = createGem("emerald", 3.0);
			InstallationResult result = slotManager.install(fullTool, gem2);

			assertFalse(result.success(), "Should fail when all slots full");
			assertTrue(result.errorMessage().isPresent(), "Should have error message");
		}
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private TestComponent createToolWithTwoGemSlots() {
		return new TestComponent(
			OpenIdentifier.of("forgero:sword"),
			Set.of(),
			Map.of(),
			ComponentUpgrades.of(List.of(
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("offensive_gem_slot"),
					OFFENSIVE_GEM_SLOT_TYPE,
					"Offensive Gem Slot"
				),
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("utility_gem_slot"),
					UTILITY_GEM_SLOT_TYPE,
					"Utility Gem Slot"
				)
			))
		);
	}

	private TestComponent createToolWithThreeGemSlots() {
		return new TestComponent(
			OpenIdentifier.of("forgero:pickaxe"),
			Set.of(),
			Map.of(),
			ComponentUpgrades.of(List.of(
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("gem_slot_1"),
					GEM_TYPE,
					"Gem Slot 1"
				),
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("gem_slot_2"),
					GEM_TYPE,
					"Gem Slot 2"
				),
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("gem_slot_3"),
					GEM_TYPE,
					"Gem Slot 3"
				)
			))
		);
	}

	private TestComponent createToolWithStrictGemSlot() {
		return new TestComponent(
			OpenIdentifier.of("forgero:axe"),
			Set.of(),
			Map.of(),
			ComponentUpgrades.of(List.of(
				ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("gem_slot"),
					GEM_TYPE, // Requires gem tag
					"Gem Slot"
				)
			))
		);
	}

	private TestComponent createOffensiveGem() {
		OpenIdentifier conditionType = OpenIdentifier.of("forgero:in_slot_type");
		InSlotTypeCondition slotCondition = new InSlotTypeCondition(conditionType, OFFENSIVE_GEM_SLOT_TYPE);
		Condition condition = new Condition(List.of(slotCondition), List.of());

		return new TestComponent(
			OpenIdentifier.of("forgero:ruby"),
			Set.of(OFFENSIVE_GEM_SLOT_TYPE),
			Map.of("attributes", List.of(
				new SimpleAttribute(
					OpenIdentifier.of("forgero:attack_damage"),
					5.0f,
					condition
				)
			)),
			ComponentUpgrades.empty()
		);
	}

	private TestComponent createUtilityGem() {
		OpenIdentifier conditionType = OpenIdentifier.of("forgero:in_slot_type");
		InSlotTypeCondition slotCondition = new InSlotTypeCondition(conditionType, UTILITY_GEM_SLOT_TYPE);
		Condition condition = new Condition(List.of(slotCondition), List.of());

		return new TestComponent(
			OpenIdentifier.of("forgero:sapphire"),
			Set.of(UTILITY_GEM_SLOT_TYPE),
			Map.of("attributes", List.of(
				new SimpleAttribute(
					OpenIdentifier.of("forgero:mining_speed"),
					2.0f,
					condition
				)
			)),
			ComponentUpgrades.empty()
		);
	}

	private TestComponent createGem(String name, double attackDamage) {
		return new TestComponent(
			OpenIdentifier.of("forgero:" + name),
			Set.of(GEM_TYPE),
			Map.of("attributes", List.of(
				new SimpleAttribute(
					OpenIdentifier.of("forgero:attack_damage"),
					(float) attackDamage
				)
			)),
			ComponentUpgrades.empty()
		);
	}

	// ============================================================
	// Test Component Implementation
	// ============================================================

	record TestComponent(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentUpgrades upgrades
	) implements CustomizableComponent {

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("forgero:test_component");
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return properties;
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return new TestComponent(id, tags, newProperties, upgrades);
		}

		@Override
		public Component withUpgrades(ComponentUpgrades newUpgrades) {
			return new TestComponent(id, tags, properties, newUpgrades);
		}

		@Override
		public List<Component> getChildren() {
			return upgrades.filledContents();
		}
	}
}
