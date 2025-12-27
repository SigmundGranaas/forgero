package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceFormatter;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DifferenceFormatterTest {

	private DifferenceFormatter formatter;

	@BeforeEach
	void setUp() {
		DifferenceFormatter.clearInverseAttributes();
		DifferenceFormatter.clearCustomStyles();
		formatter = new DifferenceFormatter();
	}

	@Test
	void neutralDifferenceReturnsEmptyText() {
		OpenIdentifier attr = OpenIdentifier.parse("forgero:attack_damage");

		var text = formatter.format(attr, 0.0001f, true);

		assertEquals("", text.getString());
	}

	@Test
	void positiveDifferenceReturnsGreen() {
		OpenIdentifier attr = OpenIdentifier.parse("forgero:attack_damage");

		Formatting color = formatter.getColor(attr, 5.0f);

		assertEquals(Formatting.GREEN, color);
	}

	@Test
	void negativeDifferenceReturnsRed() {
		OpenIdentifier attr = OpenIdentifier.parse("forgero:attack_damage");

		Formatting color = formatter.getColor(attr, -5.0f);

		assertEquals(Formatting.RED, color);
	}

	@Test
	void inverseAttributeHasInvertedColors() {
		OpenIdentifier weight = OpenIdentifier.parse("forgero:weight");
		DifferenceFormatter.registerInverseAttribute(weight);

		// For weight, lower is better, so positive diff should be red
		Formatting positiveColor = formatter.getColor(weight, 5.0f);
		Formatting negativeColor = formatter.getColor(weight, -5.0f);

		assertEquals(Formatting.RED, positiveColor); // More weight is bad
		assertEquals(Formatting.GREEN, negativeColor); // Less weight is good
	}

	@Test
	void isNeutralReturnsTrueForSmallDifferences() {
		assertTrue(formatter.isNeutral(0.0001f));
		assertTrue(formatter.isNeutral(-0.0001f));
		assertTrue(formatter.isNeutral(0f));

		assertFalse(formatter.isNeutral(0.01f));
		assertFalse(formatter.isNeutral(-0.01f));
	}

	@Test
	void isInverseAttributeCheckWorks() {
		OpenIdentifier weight = OpenIdentifier.parse("forgero:weight");
		OpenIdentifier damage = OpenIdentifier.parse("forgero:damage");

		assertFalse(DifferenceFormatter.isInverseAttribute(weight));
		assertFalse(DifferenceFormatter.isInverseAttribute(damage));

		DifferenceFormatter.registerInverseAttribute(weight);

		assertTrue(DifferenceFormatter.isInverseAttribute(weight));
		assertFalse(DifferenceFormatter.isInverseAttribute(damage));
	}
}
