package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.display.AttributeDisplayData;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.operator.*;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompositeSectionWriter implements TooltipSectionWriter {

	private static final List<AttributeDisplayData> ATTRIBUTES_TO_DISPLAY = List.of(
			new AttributeDisplayData(DefaultAttributes.ATTACK_DAMAGE, "attribute.forgero.attack_damage", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ATTACK_SPEED, "attribute.forgero.attack_speed", AttributeDisplayData.Style.BASE_VALUE, Optional.of(0.0f), -4.0f, 1.0f),
			new AttributeDisplayData(DefaultAttributes.DURABILITY, "attribute.forgero.durability", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_SPEED, "attribute.forgero.mining_speed", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_LEVEL, "attribute.forgero.mining_level", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR, "attribute.forgero.armor", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR_TOUGHNESS, "attribute.forgero.armor_toughness", AttributeDisplayData.Style.ADDITIVE, 0.0f)
	);

	private final List<Attribute> components;
	private final List<Text> cachedEntries = new ArrayList<>();

	public CompositeSectionWriter(Component component, List<Attribute> attributes) {
		this.components = attributes;
	}

	@Override
	public void append(List<Text> tooltip, TooltipContext context) {
		if (!cachedEntries.isEmpty()) {
			tooltip.add(TooltipTextFormatter.createSectionHeader("tooltip.forgero.component_attributes"));
			tooltip.addAll(cachedEntries);
			tooltip.add(Text.of("")); // Padding
		}
	}

	@Override
	public boolean shouldShow() {
		if (components.isEmpty()) {
			return false;
		}
		cachedEntries.clear();

		Map<OpenIdentifier, AttributeDisplayData> displayDataMap = ATTRIBUTES_TO_DISPLAY.stream()
				.collect(Collectors.toMap(AttributeDisplayData::id, Function.identity()));

		// 1. Segregate components by operator type
		List<Attribute> additives = components.stream()
				.filter(c -> c.operator() instanceof AdditionOperator || c.operator() instanceof SubtractionOperator)
				.toList();

		List<Attribute> multiplicatives = components.stream()
				.filter(c -> c.operator() instanceof MultiplicationOperator || c.operator() instanceof DivisionOperator)
				.toList();

		// 2. Aggregate values for each category
		Map<OpenIdentifier, Float> aggregatedAdditives = aggregateAdditives(additives);
		Map<OpenIdentifier, Float> aggregatedMultiplicatives = aggregateMultiplicatives(multiplicatives);

		// 3. Build tooltip sections
		buildSection("tooltip.forgero.base_values", aggregatedAdditives, displayDataMap, this::createBaseValueLine);
		buildSection("tooltip.forgero.multipliers", aggregatedMultiplicatives, displayDataMap, this::createMultiplierLine);

		return !cachedEntries.isEmpty();
	}

	private Map<OpenIdentifier, Float> aggregateAdditives(List<Attribute> components) {
		return components.stream()
				.collect(Collectors.groupingBy(
						Attribute::type,
						Collectors.summingDouble(comp -> {
							float value = comp.value();
							return comp.operator() instanceof SubtractionOperator ? -value : value;
						})
				)).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().floatValue()));
	}

	private Map<OpenIdentifier, Float> aggregateMultiplicatives(List<Attribute> components) {
		return components.stream()
				.collect(Collectors.groupingBy(
						Attribute::type,
						Collectors.reducing(1.0f, comp -> {
							float value = comp.value();
							return comp.operator() instanceof DivisionOperator ? 1 / value : value;
						}, (a, b) -> a * b)
				));
	}

	private void buildSection(String titleKey, Map<OpenIdentifier, Float> aggregatedValues, Map<OpenIdentifier, AttributeDisplayData> displayDataMap, TriFunction<AttributeDisplayData, Float, List<Text>> lineBuilder) {
		List<Text> sectionLines = new ArrayList<>();
		aggregatedValues.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.comparing(OpenIdentifier::toString)))
				.forEach(entry -> {
					AttributeDisplayData displayData = displayDataMap.get(entry.getKey());
					if (displayData != null) {
						lineBuilder.apply(displayData, entry.getValue(), sectionLines);
					}
				});

		if (!sectionLines.isEmpty()) {
			cachedEntries.add(Text.literal(" ").append(Text.translatable(titleKey).formatted(Formatting.GRAY)).append(":"));
			cachedEntries.addAll(sectionLines);
		}
	}

	private void createBaseValueLine(AttributeDisplayData displayData, float value, List<Text> lines) {
		if (Math.abs(value) < 0.001f) return;
		MutableText valueText = TooltipTextFormatter.createValueText(value, Formatting.GREEN, Formatting.RED, value > 0 ? "+" : "", "");
		lines.add(TooltipTextFormatter.createAttributeLine(displayData.translationKey(), valueText));
	}

	private void createMultiplierLine(AttributeDisplayData displayData, float value, List<Text> lines) {
		if (Math.abs(value - 1.0) < 0.001f) return;
		MutableText valueText = TooltipTextFormatter.createValueText(value, Formatting.GOLD, Formatting.RED, "x", "");
		lines.add(TooltipTextFormatter.createAttributeLine(displayData.translationKey(), valueText));
	}

	// Functional interface for the line builder methods
	@FunctionalInterface
	private interface TriFunction<A, B, C> {
		void apply(A a, B b, C c);
	}
}
