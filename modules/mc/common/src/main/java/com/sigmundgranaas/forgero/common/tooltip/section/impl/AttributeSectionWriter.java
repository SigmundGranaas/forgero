package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.comparison.ComparisonContext;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceCalculator;
import com.sigmundgranaas.forgero.common.tooltip.display.AttributeDisplayData;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;

/**
 * Writes the attributes section with comparison support.
 * <p>
 * Displays item statistics (damage, speed, durability, etc.) with:
 * <ul>
 *   <li>Colored values based on comparison (green for better, red for worse)</li>
 *   <li>Arrow indicators for changes</li>
 *   <li>Hiding of default/zero values when configured</li>
 * </ul>
 */
public class AttributeSectionWriter extends AbstractSectionWriter {

	/**
	 * Default attributes to display, in order.
	 */
	private static final List<AttributeDisplayData> DEFAULT_ATTRIBUTES = List.of(
			new AttributeDisplayData(
					DefaultAttributes.ATTACK_DAMAGE,
					"attribute.forgero.attack_damage",
					AttributeDisplayData.Style.ADDITIVE,
					0.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.ATTACK_SPEED,
					"attribute.forgero.attack_speed",
					AttributeDisplayData.Style.BASE_VALUE,
					Optional.of(0.0f),
					-4.0f, // Minecraft displays attack speed as (4 + value)
					1.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.DURABILITY,
					"attribute.forgero.durability",
					AttributeDisplayData.Style.BASE_VALUE,
					0.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.MINING_SPEED,
					"attribute.forgero.mining_speed",
					AttributeDisplayData.Style.BASE_VALUE,
					0.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.MINING_LEVEL,
					"attribute.forgero.mining_level",
					AttributeDisplayData.Style.BASE_VALUE,
					0.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.ARMOR,
					"attribute.forgero.armor",
					AttributeDisplayData.Style.ADDITIVE,
					0.0f
			),
			new AttributeDisplayData(
					DefaultAttributes.ARMOR_TOUGHNESS,
					"attribute.forgero.armor_toughness",
					AttributeDisplayData.Style.ADDITIVE,
					0.0f
			)
	);

	public AttributeSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		for (AttributeDisplayData displayData : DEFAULT_ATTRIBUTES) {
			float rawValue = context.attributes().getValue(displayData.id());

			// Skip default values if configured
			if (displayData.isDefault(rawValue) && context.hideZeroValues()) {
				continue;
			}

			// Skip zero values if configured
			if (rawValue == 0 && context.hideZeroValues()) {
				continue;
			}

			float displayValue = displayData.getDisplayValue(rawValue);
			MutableText line = createAttributeLine(displayData, displayValue);

			// Add comparison indicator if we have a comparison context
			if (!(context.comparisonContext() instanceof ComparisonContext.None)) {
				appendComparisonIndicator(line, displayData.id(), rawValue);
			}

			addLine(line);
		}
	}

	private MutableText createAttributeLine(AttributeDisplayData data, float displayValue) {
		String valueStr = formatValue(data, displayValue);

		return indented(1)
				.append(Text.translatable(data.translationKey()).formatted(Formatting.DARK_GREEN))
				.append(": ")
				.append(Text.literal(valueStr).formatted(Formatting.WHITE));
	}

	private String formatValue(AttributeDisplayData data, float displayValue) {
		String formatted = TooltipTextFormatter.formatFloat(displayValue);

		return switch (data.style()) {
			case ADDITIVE -> (displayValue >= 0 ? "+" : "") + formatted;
			case PERCENTAGE -> formatted + "%";
			case BASE_VALUE -> formatted;
		};
	}

	private void appendComparisonIndicator(MutableText line, OpenIdentifier attributeId, float currentRawValue) {
		Optional<DifferenceCalculator.AttributeDifference> diffOpt =
				context.differenceCalculator().calculateFromResult(
						context.attributes(),
						attributeId,
						context.comparisonContext()
				);

		diffOpt.ifPresent(diff -> {
			if (!diff.isNeutral()) {
				line.append(" ");
				line.append(context.differenceFormatter().format(
						attributeId,
						diff.difference(),
						context.showDifferenceValues()
				));
			}
		});
	}
}
