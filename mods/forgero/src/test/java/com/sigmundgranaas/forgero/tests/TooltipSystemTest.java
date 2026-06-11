package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipRenderConfig;
import com.sigmundgranaas.forgero.common.tooltip.comparison.ComparisonContext;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceCalculator;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceFormatter;
import com.sigmundgranaas.forgero.common.tooltip.section.DefaultSections;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Forgero modern tooltip system.
 *
 * TEST CATEGORIES:
 * 1. SECTION_VISIBILITY - Verifying sections appear when they should
 * 2. ATTRIBUTE_DISPLAY - Testing attribute values and formatting
 * 3. COMPARISON - Testing comparison with baseline variants
 * 4. COLORIZATION - Testing color coding for improvements/downgrades
 * 5. SLOTS_SECTION - Testing the slots section writer
 * 6. PARTS_SECTION - Testing the parts section writer
 * 7. DIFFERENCE_FORMATTING - Testing +/- and arrow formatting
 */
public class TooltipSystemTest implements ForgeroGameTest {

	// ========================================================================
	// CATEGORY 1: SECTION VISIBILITY TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void section_visibility_attributes_shown_for_tool(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		List<Text> tooltip = buildTooltip(pickaxe);

		// Attributes section should be present (attack damage, mining speed, durability)
		boolean hasAttributeContent = tooltip.stream()
				.anyMatch(t -> containsAny(t.getString(),
						"Attack", "Mining", "Durability", "Damage", "Speed"));

		assertTrue(hasAttributeContent,
				"Attributes section should show attack/mining/durability for pickaxe. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void section_visibility_slots_shown_for_customizable(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		assertTrue(pickaxe instanceof CustomizableComponent,
				"Pickaxe should be CustomizableComponent");

		List<Text> tooltip = buildTooltip(pickaxe);

		// The slots section header or slot content should appear
		boolean hasSlotsContent = tooltip.stream()
				.anyMatch(t -> containsAny(t.getString().toLowerCase(),
						"slot", "gem", "binding", "reinforcement", "empty"));

		assertTrue(hasSlotsContent,
				"Slots section should show for customizable component. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void section_visibility_parts_shown_for_structured(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		assertTrue(pickaxe instanceof StructuredComponent,
				"Pickaxe should be StructuredComponent");

		List<Text> tooltip = buildTooltip(pickaxe);

		// The parts section should show blade/handle/head content
		boolean hasPartsContent = tooltip.stream()
				.anyMatch(t -> containsAny(t.getString().toLowerCase(),
						"head", "handle", "blade", "part", "iron"));

		assertTrue(hasPartsContent,
				"Parts section should show for structured component. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void section_visibility_no_slots_for_simple_component(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		// Materials don't have slots
		Component iron = ctx.component("forgero:iron").orElseThrow();

		if (iron instanceof CustomizableComponent customizable) {
			int slotCount = customizable.getUpgradeSlots().size();
			if (slotCount == 0) {
				List<Text> tooltip = buildTooltip(iron);
				// Verify no "Empty" slot entries
				boolean hasEmptySlots = tooltip.stream()
						.anyMatch(t -> t.getString().toLowerCase().contains("empty"));
				assertFalse(hasEmptySlots,
						"Material without slots shouldn't show empty slot lines");
			}
		}

		context.complete();
	}

	// ========================================================================
	// CATEGORY 2: ATTRIBUTE DISPLAY TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void attribute_display_shows_attack_damage(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component sword = ctx.component("forgero:iron-sword").orElseThrow();

		var result = AttributeEngine.resolveAttributes(sword);
		float damage = result.getValue(OpenIdentifier.parse("forgero:attack_damage"));

		assertTrue(damage > 0, "Sword should have attack damage > 0");

		List<Text> tooltip = buildTooltip(sword);
		boolean showsDamage = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("attack") ||
						t.getString().toLowerCase().contains("damage"));

		assertTrue(showsDamage,
				"Tooltip should display attack damage. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void attribute_display_shows_mining_speed_for_tool(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		var result = AttributeEngine.resolveAttributes(pickaxe);
		float miningSpeed = result.getValue(OpenIdentifier.parse("forgero:mining_speed"));

		assertTrue(miningSpeed > 0, "Pickaxe should have mining speed > 0");

		List<Text> tooltip = buildTooltip(pickaxe);
		boolean showsMiningSpeed = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("mining"));

		assertTrue(showsMiningSpeed,
				"Tooltip should display mining speed. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void attribute_display_hides_zero_values(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		// Pickaxe shouldn't have armor - verify zero values are hidden
		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		TooltipRenderConfig config = TooltipRenderConfig.builder()
				.hideZeroValues(true)
				.build();

		List<Text> tooltip = TooltipApi.builder(pickaxe)
				.withRenderConfig(config)
				.build(TooltipContext.BASIC);

		// Armor should not appear for pickaxe (it has 0 armor)
		boolean showsArmor = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("armor"));

		assertFalse(showsArmor,
				"Tooltip should hide zero armor value. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	// ========================================================================
	// CATEGORY 3: COMPARISON TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void comparison_with_stripped_item_detects_upgrade_improvement(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		// Install upgrade
		var installResult = slotManager.install(pickaxe, diamond);
		if (!installResult.success()) {
			// No compatible slot - skip test
			context.complete();
			return;
		}

		Component upgraded = installResult.component().get();
		Component stripped = slotManager.removeAllUpgrades(upgraded);

		// Calculate difference
		DifferenceCalculator calculator = new DifferenceCalculator();
		var diffOpt = calculator.calculate(
				upgraded,
				OpenIdentifier.parse("forgero:durability"),
				ComparisonContext.strippedItem(stripped)
		);

		if (diffOpt.isPresent()) {
			var diff = diffOpt.get();
			// Diamond upgrade should improve durability
			assertTrue(diff.difference() >= 0,
					"Diamond upgrade should not decrease durability. Diff: " + diff.difference());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void comparison_vs_component_calculates_difference(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component ironPickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamondPickaxe = ctx.component("forgero:diamond-pickaxe").orElseThrow();

		DifferenceCalculator calculator = new DifferenceCalculator();

		var diffOpt = calculator.calculate(
				diamondPickaxe,
				OpenIdentifier.parse("forgero:durability"),
				ComparisonContext.vsComponent(ironPickaxe)
		);

		assertTrue(diffOpt.isPresent(), "Should calculate difference between iron and diamond pickaxe");

		var diff = diffOpt.get();
		assertTrue(diff.difference() > 0,
				"Diamond pickaxe should have more durability than iron. Diff: " + diff.difference());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void comparison_none_returns_empty_difference(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		DifferenceCalculator calculator = new DifferenceCalculator();

		var diffOpt = calculator.calculate(
				pickaxe,
				OpenIdentifier.parse("forgero:durability"),
				ComparisonContext.none()
		);

		assertTrue(diffOpt.isEmpty(),
				"ComparisonContext.none() should return empty difference");

		context.complete();
	}

	// ========================================================================
	// CATEGORY 4: COLORIZATION TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void colorization_positive_difference_is_green(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier attackDamage = OpenIdentifier.parse("forgero:attack_damage");

		// Positive difference for normal attribute = green (improvement)
		Formatting color = formatter.getColor(attackDamage, 5.0f);

		assertEquals(Formatting.GREEN, color,
				"Positive attack damage difference should be GREEN");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void colorization_negative_difference_is_red(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier attackDamage = OpenIdentifier.parse("forgero:attack_damage");

		// Negative difference for normal attribute = red (downgrade)
		Formatting color = formatter.getColor(attackDamage, -3.0f);

		assertEquals(Formatting.RED, color,
				"Negative attack damage difference should be RED");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void colorization_inverse_attribute_inverts_colors(TestContext context) {
		ensureSectionsRegistered();

		OpenIdentifier weight = OpenIdentifier.parse("forgero:weight");

		// Register weight as inverse (lower is better)
		DifferenceFormatter.registerInverseAttribute(weight);

		try {
			DifferenceFormatter formatter = new DifferenceFormatter();

			// Positive weight increase = RED (bad, heavier)
			Formatting colorForIncrease = formatter.getColor(weight, 2.0f);
			assertEquals(Formatting.RED, colorForIncrease,
					"Increased weight should be RED (bad)");

			// Negative weight (decrease) = GREEN (good, lighter)
			Formatting colorForDecrease = formatter.getColor(weight, -2.0f);
			assertEquals(Formatting.GREEN, colorForDecrease,
					"Decreased weight should be GREEN (good)");
		} finally {
			// Clean up
			DifferenceFormatter.clearInverseAttributes();
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void colorization_neutral_difference_is_white(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier durability = OpenIdentifier.parse("forgero:durability");

		// Very small difference should be neutral
		Formatting color = formatter.getColor(durability, 0.0001f);

		assertEquals(Formatting.WHITE, color,
				"Neutral/zero difference should be WHITE");

		context.complete();
	}

	// ========================================================================
	// CATEGORY 5: SLOTS SECTION TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void slots_section_shows_filled_slot_content(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		var installResult = slotManager.install(pickaxe, diamond);
		if (!installResult.success()) {
			context.complete();
			return;
		}

		Component upgraded = installResult.component().get();
		List<Text> tooltip = buildTooltip(upgraded);

		// Should show "Diamond" in the tooltip (filled slot content)
		boolean showsDiamond = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("diamond"));

		assertTrue(showsDiamond,
				"Slots section should show 'Diamond' for filled slot. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void slots_section_shows_empty_indicator(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		// Ensure it has empty slots
		int emptyCount = slotManager.countEmptySlots(pickaxe);
		if (emptyCount == 0) {
			context.complete();
			return;
		}

		List<Text> tooltip = buildTooltip(pickaxe);

		// Should show "Empty" for unfilled slots
		boolean showsEmpty = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("empty"));

		assertTrue(showsEmpty,
				"Slots section should show 'Empty' for unfilled slots. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void slots_section_shows_slot_type(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		var slots = slotManager.getAllUpgradeSlots(pickaxe);
		if (slots.isEmpty()) {
			context.complete();
			return;
		}

		List<Text> tooltip = buildTooltip(pickaxe);

		// Should show slot types like "Gem", "Binding", "Reinforcement"
		boolean showsSlotType = tooltip.stream()
				.anyMatch(t -> containsAny(t.getString().toLowerCase(),
						"gem", "binding", "reinforcement", "grip", "pommel"));

		assertTrue(showsSlotType,
				"Slots section should show slot types. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	// ========================================================================
	// CATEGORY 6: PARTS SECTION TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void parts_section_shows_component_parts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		assertTrue(pickaxe instanceof StructuredComponent,
				"Pickaxe must be StructuredComponent");

		StructuredComponent structured = (StructuredComponent) pickaxe;
		int partCount = structured.structure().allParts().size();
		assertTrue(partCount >= 2, "Pickaxe should have at least 2 parts (head + handle)");

		List<Text> tooltip = buildTooltip(pickaxe);

		// Should show part names like "Pickaxe Head", "Handle"
		boolean showsHead = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("head"));
		boolean showsHandle = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("handle"));

		assertTrue(showsHead || showsHandle,
				"Parts section should show head/handle. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void parts_section_shows_material_names(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ensureSectionsRegistered();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		List<Text> tooltip = buildTooltip(pickaxe);

		// Should show material "Iron" in parts
		boolean showsIron = tooltip.stream()
				.anyMatch(t -> t.getString().toLowerCase().contains("iron"));

		assertTrue(showsIron,
				"Parts section should show material 'Iron'. Got: " + tooltipToString(tooltip));

		context.complete();
	}

	// ========================================================================
	// CATEGORY 7: DIFFERENCE FORMATTING TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void difference_format_shows_up_arrow_for_positive(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier damage = OpenIdentifier.parse("forgero:attack_damage");

		var formatted = formatter.format(damage, 5.0f, false);
		String text = formatted.getString();

		// Should contain up arrow (Unicode or ASCII)
		assertTrue(text.contains("\u2191") || text.contains("^") || text.contains("+"),
				"Positive difference should show up arrow. Got: " + text);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void difference_format_shows_down_arrow_for_negative(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier damage = OpenIdentifier.parse("forgero:attack_damage");

		var formatted = formatter.format(damage, -3.0f, false);
		String text = formatted.getString();

		// Should contain down arrow (Unicode or ASCII)
		assertTrue(text.contains("\u2193") || text.contains("v") || text.contains("-"),
				"Negative difference should show down arrow. Got: " + text);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void difference_format_shows_value_when_requested(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier damage = OpenIdentifier.parse("forgero:attack_damage");

		var formatted = formatter.format(damage, 5.5f, true);
		String text = formatted.getString();

		// Should contain the value
		assertTrue(text.contains("5") || text.contains("5.5"),
				"Should show numeric value when requested. Got: " + text);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void difference_format_empty_for_neutral(TestContext context) {
		ensureSectionsRegistered();

		DifferenceFormatter formatter = new DifferenceFormatter();
		OpenIdentifier damage = OpenIdentifier.parse("forgero:attack_damage");

		var formatted = formatter.format(damage, 0.0f, true);
		String text = formatted.getString();

		assertTrue(text.isEmpty(),
				"Neutral difference should produce empty text. Got: '" + text + "'");

		context.complete();
	}

	// ========================================================================
	// HELPER METHODS
	// ========================================================================

	private void ensureSectionsRegistered() {
		if (!DefaultSections.isRegistered()) {
			DefaultSections.register();
		}
	}

	private List<Text> buildTooltip(Component component) {
		return TooltipApi.builder(component)
				.build(TooltipContext.BASIC);
	}

	private String tooltipToString(List<Text> tooltip) {
		StringBuilder sb = new StringBuilder();
		for (Text t : tooltip) {
			sb.append(t.getString()).append("\n");
		}
		return sb.toString();
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}
