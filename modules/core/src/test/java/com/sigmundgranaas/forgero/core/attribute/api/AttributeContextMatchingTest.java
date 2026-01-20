package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AttributeContext#matchesSlotContext} to verify correct filtering
 * of attributes in upgrade slots.
 *
 * <p>This test was created to catch a critical bug where part-composite context
 * attributes were incorrectly being applied to upgrade slots, causing materials
 * like iron to add +10 damage when used as upgrades instead of the intended +2.</p>
 */
@DisplayName("AttributeContext.matchesSlotContext Tests")
class AttributeContextMatchingTest {

	private static final OpenIdentifier OFFENSIVE_CONTEXT = new OpenIdentifier("forgero", "contexts/offensive");
	private static final OpenIdentifier DEFENSIVE_CONTEXT = new OpenIdentifier("forgero", "contexts/defensive");

	@Nested
	@DisplayName("Attributes with no context")
	class NoContextAttributes {

		@Test
		@DisplayName("No-context attributes always match any slot")
		void noContextMatchesAnySlot() {
			assertTrue(AttributeContext.matchesSlotContext(Optional.empty(), Optional.empty()),
					"No-context should match slot with no context");
			assertTrue(AttributeContext.matchesSlotContext(Optional.empty(), Optional.of(OFFENSIVE_CONTEXT)),
					"No-context should match slot with offensive context");
		}
	}

	@Nested
	@DisplayName("UPGRADE context attributes")
	class UpgradeContextAttributes {

		@Test
		@DisplayName("UPGRADE context matches any upgrade slot")
		void upgradeContextMatchesAnySlot() {
			Optional<OpenIdentifier> upgradeCtx = Optional.of(AttributeContext.UPGRADE);

			assertTrue(AttributeContext.matchesSlotContext(upgradeCtx, Optional.empty()),
					"UPGRADE context should match slot with no context");
			assertTrue(AttributeContext.matchesSlotContext(upgradeCtx, Optional.of(OFFENSIVE_CONTEXT)),
					"UPGRADE context should match slot with offensive context");
		}
	}

	@Nested
	@DisplayName("PART_COMPOSITE context attributes - CRITICAL BUG TEST")
	class PartCompositeContextAttributes {

		@Test
		@DisplayName("PART_COMPOSITE context NEVER matches upgrade slots - bug fix verification")
		void partCompositeNeverMatchesUpgradeSlots() {
			Optional<OpenIdentifier> partCompositeCtx = Optional.of(AttributeContext.PART_COMPOSITE);

			assertFalse(AttributeContext.matchesSlotContext(partCompositeCtx, Optional.empty()),
					"PART_COMPOSITE should NOT match slot with no context - this was the bug!");
			assertFalse(AttributeContext.matchesSlotContext(partCompositeCtx, Optional.of(OFFENSIVE_CONTEXT)),
					"PART_COMPOSITE should NOT match slot with offensive context");
			assertFalse(AttributeContext.matchesSlotContext(partCompositeCtx, Optional.of(AttributeContext.PART_COMPOSITE)),
					"PART_COMPOSITE should NOT even match slot with same context (upgrades don't use composition)");
		}

		@Test
		@DisplayName("Bug scenario: Iron reinforcement should not add part-composite damage")
		void ironReinforcementBugScenario() {
			// Iron material has attack_damage: 4.0 with context: "forgero:part-composite"
			// When installed as a reinforcement upgrade, this should NOT apply
			Optional<OpenIdentifier> ironPartCompositeDamage = Optional.of(AttributeContext.PART_COMPOSITE);

			// Reinforcement slot might have offensive context or no context
			assertFalse(AttributeContext.matchesSlotContext(ironPartCompositeDamage, Optional.of(OFFENSIVE_CONTEXT)),
					"Iron's part-composite damage should NOT apply in offensive context slot");
			assertFalse(AttributeContext.matchesSlotContext(ironPartCompositeDamage, Optional.empty()),
					"Iron's part-composite damage should NOT apply in slot with no context");
		}
	}

	@Nested
	@DisplayName("EQUIPMENT_COMPOSITE context attributes")
	class EquipmentCompositeContextAttributes {

		@Test
		@DisplayName("EQUIPMENT_COMPOSITE context never matches upgrade slots")
		void equipmentCompositeNeverMatchesUpgradeSlots() {
			Optional<OpenIdentifier> equipCompositeCtx = Optional.of(AttributeContext.EQUIPMENT_COMPOSITE);

			assertFalse(AttributeContext.matchesSlotContext(equipCompositeCtx, Optional.empty()),
					"EQUIPMENT_COMPOSITE should NOT match slot with no context");
			assertFalse(AttributeContext.matchesSlotContext(equipCompositeCtx, Optional.of(OFFENSIVE_CONTEXT)),
					"EQUIPMENT_COMPOSITE should NOT match slot with offensive context");
		}
	}

	@Nested
	@DisplayName("Specific context attributes")
	class SpecificContextAttributes {

		@Test
		@DisplayName("Specific context only matches when slot context matches exactly")
		void specificContextRequiresExactMatch() {
			Optional<OpenIdentifier> offensiveCtx = Optional.of(OFFENSIVE_CONTEXT);

			assertTrue(AttributeContext.matchesSlotContext(offensiveCtx, Optional.of(OFFENSIVE_CONTEXT)),
					"Offensive context should match slot with offensive context");
			assertFalse(AttributeContext.matchesSlotContext(offensiveCtx, Optional.of(DEFENSIVE_CONTEXT)),
					"Offensive context should NOT match slot with defensive context");
		}

		@Test
		@DisplayName("Specific context does NOT match slot with no context")
		void specificContextDoesNotMatchEmptySlot() {
			Optional<OpenIdentifier> offensiveCtx = Optional.of(OFFENSIVE_CONTEXT);

			assertFalse(AttributeContext.matchesSlotContext(offensiveCtx, Optional.empty()),
					"Offensive context should NOT match slot with no context filter");
		}
	}

	@Nested
	@DisplayName("Slot with no context filter")
	class EmptySlotContext {

		@Test
		@DisplayName("Slot with no context only accepts no-context and UPGRADE attributes")
		void emptySlotOnlyAcceptsNoContextAndUpgrade() {
			Optional<OpenIdentifier> emptySlot = Optional.empty();

			// Should accept
			assertTrue(AttributeContext.matchesSlotContext(Optional.empty(), emptySlot),
					"No-context attribute should pass through unfiltered slot");
			assertTrue(AttributeContext.matchesSlotContext(Optional.of(AttributeContext.UPGRADE), emptySlot),
					"UPGRADE context attribute should pass through unfiltered slot");

			// Should reject
			assertFalse(AttributeContext.matchesSlotContext(Optional.of(AttributeContext.PART_COMPOSITE), emptySlot),
					"PART_COMPOSITE should NOT pass through unfiltered slot");
			assertFalse(AttributeContext.matchesSlotContext(Optional.of(AttributeContext.EQUIPMENT_COMPOSITE), emptySlot),
					"EQUIPMENT_COMPOSITE should NOT pass through unfiltered slot");
			assertFalse(AttributeContext.matchesSlotContext(Optional.of(OFFENSIVE_CONTEXT), emptySlot),
					"Specific context should NOT pass through unfiltered slot");
		}
	}

	@Nested
	@DisplayName("Real-world material scenarios")
	class RealWorldScenarios {

		@Test
		@DisplayName("Iron used as reinforcement: only upgrade-context attributes apply")
		void ironAsReinforcement() {
			// Iron has:
			// - attack_damage: 4.0 (context: part-composite) - should NOT apply
			// - attack_damage: 2.0 (context: contexts/offensive from metal_upgrade_base) - should apply IF slot has offensive context

			Optional<OpenIdentifier> partComposite = Optional.of(AttributeContext.PART_COMPOSITE);
			Optional<OpenIdentifier> offensive = Optional.of(OFFENSIVE_CONTEXT);
			Optional<OpenIdentifier> upgrade = Optional.of(AttributeContext.UPGRADE);

			// Reinforcement slot with offensive context
			Optional<OpenIdentifier> offensiveSlot = Optional.of(OFFENSIVE_CONTEXT);

			assertFalse(AttributeContext.matchesSlotContext(partComposite, offensiveSlot),
					"Iron's 4.0 part-composite damage should NOT apply");
			assertTrue(AttributeContext.matchesSlotContext(offensive, offensiveSlot),
					"Iron's 2.0 offensive-context damage SHOULD apply");
			assertTrue(AttributeContext.matchesSlotContext(upgrade, offensiveSlot),
					"Any upgrade-context attributes SHOULD apply");
		}

		@Test
		@DisplayName("Gem used as upgrade: only no-context and upgrade-context apply")
		void gemAsUpgrade() {
			// A gem might have:
			// - +3 damage (context: upgrade) - should apply
			// - +1 luck (no context) - should apply

			Optional<OpenIdentifier> upgrade = Optional.of(AttributeContext.UPGRADE);
			Optional<OpenIdentifier> noContext = Optional.empty();

			// Gem slot typically has no context
			Optional<OpenIdentifier> gemSlot = Optional.empty();

			assertTrue(AttributeContext.matchesSlotContext(upgrade, gemSlot),
					"Gem's upgrade-context damage should apply");
			assertTrue(AttributeContext.matchesSlotContext(noContext, gemSlot),
					"Gem's no-context luck should apply");
		}
	}
}
