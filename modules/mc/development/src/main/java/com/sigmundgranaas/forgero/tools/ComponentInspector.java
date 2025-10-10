package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.display.AttributeDisplayData;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.property.api.Resolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for generating a detailed, human-readable report
 * of a Forgero Component's structure, stats, and properties.
 */
public class ComponentInspector {
	private static final List<AttributeDisplayData> ATTRIBUTES_TO_DISPLAY = List.of(
			new AttributeDisplayData(DefaultAttributes.ATTACK_DAMAGE, "attribute.forgero.attack_damage", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ATTACK_SPEED, "attribute.forgero.attack_speed", AttributeDisplayData.Style.BASE_VALUE, Optional.of(0.0f), -4.0f, 1.0f),
			new AttributeDisplayData(DefaultAttributes.DURABILITY, "attribute.forgero.durability", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_SPEED, "attribute.forgero.mining_speed", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_LEVEL, "attribute.forgero.mining_level", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR, "attribute.forgero.armor", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR_TOUGHNESS, "attribute.forgero.armor_toughness", AttributeDisplayData.Style.ADDITIVE, 0.0f)
	);

	public String generateReport(Component component, Resolver resolver) {
		StringBuilder builder = new StringBuilder();
		String header = "COMPONENT REPORT: " + component.id();
		String separator = "=".repeat(header.length() + 4);

		builder.append(separator).append("\n  ").append(header).append("\n").append(separator).append("\n\n");
		appendFinalAttributes(builder, component, resolver);
		builder.append("\n");
		appendCompositionTree(builder, component);
		builder.append("\n");
		appendAttributeBreakdown(builder, component, resolver);

		return builder.toString();
	}

	private void appendFinalAttributes(StringBuilder builder, Component component, Resolver resolver) {
		builder.append("-- FINAL CALCULATED STATS --\n");
		AttributeQueryResult attributes = resolver.resolve(component, new AttributeEngine());

		for (AttributeDisplayData displayData : ATTRIBUTES_TO_DISPLAY) {
			float rawValue = attributes.getValue(displayData.id());
			if (displayData.isDefault(rawValue) && rawValue == 0f) {
				continue;
			}
			float displayValue = displayData.getDisplayValue(rawValue);
			String name = formatAttributeName(displayData.id().path());
			builder.append(String.format("- %s: %s\n", name, TooltipTextFormatter.formatFloat(displayValue)));
		}
	}

	private void appendCompositionTree(StringBuilder builder, Component component) {
		builder.append("-- COMPOSITION --\n");
		appendTreeRecursive(builder, component, "", true);
	}

	private void appendTreeRecursive(StringBuilder builder, Component component, String prefix, boolean isRoot) {
		if (isRoot) {
			builder.append(component.id()).append("\n");
		}

		List<Component> structureChildren = (component instanceof StructuredComponent s) ? s.structure().children() : Collections.emptyList();
		List<Component> upgradeChildren = (component instanceof CustomizableComponent c) ? c.getUpgradeSlots().stream().flatMap(s -> s.content().stream()).toList() : Collections.emptyList();
		List<Component> allChildren = Stream.concat(structureChildren.stream(), upgradeChildren.stream()).toList();

		List<UpgradeSlot> upgradeSlots = (component instanceof CustomizableComponent c) ? c.getUpgradeSlots() : Collections.emptyList();
		List<StructureSlot> structureSlots = (component instanceof StructuredComponent s) ? new ArrayList<>(s.structure().slots().values()) : Collections.emptyList();


		int totalSlots = upgradeSlots.size() + structureSlots.size();
		int currentSlotIndex = 0;

		for (StructureSlot slot : structureSlots) {
			boolean isLast = ++currentSlotIndex == totalSlots;
			builder.append(prefix).append(isLast ? "└─ " : "├─ ").append("Slot: ").append(slot.id()).append("\n");
			appendTreeRecursive(builder, slot.content(), prefix + (isLast ? "   " : "│  "), false);
		}

		for (UpgradeSlot slot : upgradeSlots) {
			boolean isLast = ++currentSlotIndex == totalSlots;
			builder.append(prefix).append(isLast ? "└─ " : "├─ ").append("Slot: ").append(slot.id());
			slot.content().ifPresentOrElse(
					content -> {
						builder.append("\n");
						appendTreeRecursive(builder, content, prefix + (isLast ? "   " : "│  "), false);
					},
					() -> builder.append(" [Empty]\n")
			);
		}
		if (!isRoot) {
			builder.insert(builder.lastIndexOf("\n") + 1, prefix + "└─ " + component.id() + "\n");
		}
	}

	private void appendAttributeBreakdown(StringBuilder builder, Component component, Resolver resolver) {
		builder.append("-- ATTRIBUTE BREAKDOWN --\n");
		AttributeQueryResult finalAttributes = resolver.resolve(component, new AttributeEngine());
		List<Component> allComponents = traverse(component);

		for (AttributeDisplayData displayData : ATTRIBUTES_TO_DISPLAY) {
			float finalValue = finalAttributes.getValue(displayData.id());
			if (displayData.isDefault(finalValue) && finalValue == 0f) {
				continue;
			}
			float displayValue = displayData.getDisplayValue(finalValue);
			String name = formatAttributeName(displayData.id().path());

			List<String> parts = new ArrayList<>();
			for (Component part : allComponents) {
				float partValue = getRawAttributeValue(part, displayData.id());
				if (partValue != 0) {
					parts.add(String.format("%s [%s]", formatOperatorValue(partValue, displayData), part.id().path()));
				}
			}

			if (parts.isEmpty()) continue;

			builder.append(String.format("- %s: %s = (%s)\n", name, TooltipTextFormatter.formatFloat(displayValue), String.join(" ", parts)));
		}
	}

	private float getRawAttributeValue(Component component, OpenIdentifier attributeId) {
		return (float) component.properties(Attribute.KEY).stream()
				.filter(attr -> attr.type().equals(attributeId))
				.mapToDouble(attr -> {
					if (attr.operator() instanceof MultiplicationOperator) {
						// Normalize multipliers to be additive percentages for display
						return (attr.value() - 1) * 100;
					}
					return attr.value();
				})
				.sum();
	}

	private String formatAttributeName(String path) {
		return Arrays.stream(path.split("_"))
				.map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
				.collect(Collectors.joining(" "));
	}

	private String formatOperatorValue(float value, AttributeDisplayData displayData) {
		if (displayData.style() == AttributeDisplayData.Style.PERCENTAGE || displayData.id().path().contains("multiplier")) {
			return String.format("%s%.0f%%", value > 0 ? "+" : "", value);
		}
		return String.format("%s%s", value > 0 ? "+" : "", TooltipTextFormatter.formatFloat(value));
	}


	private List<Component> traverse(Component component) {
		List<Component> allComponents = new ArrayList<>();
		Deque<Component> stack = new ArrayDeque<>();
		stack.push(component);

		while (!stack.isEmpty()) {
			Component current = stack.pop();
			allComponents.add(current);
			List<Component> children = current.getChildren();
			for (int i = children.size() - 1; i >= 0; i--) {
				stack.push(children.get(i));
			}
		}
		return allComponents;
	}
}
