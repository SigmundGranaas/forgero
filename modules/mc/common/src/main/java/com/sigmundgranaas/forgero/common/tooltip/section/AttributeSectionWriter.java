package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.tooltip.display.AttributeDisplayData;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttributeSectionWriter implements TooltipSectionWriter {

	private static final List<AttributeDisplayData> ATTRIBUTES_TO_DISPLAY = List.of(
			new AttributeDisplayData(DefaultAttributes.ATTACK_DAMAGE, "attribute.forgero.attack_damage", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ATTACK_SPEED, "attribute.forgero.attack_speed", AttributeDisplayData.Style.BASE_VALUE, Optional.of(0.0f), -4.0f, 1.0f),
			new AttributeDisplayData(DefaultAttributes.DURABILITY, "attribute.forgero.durability", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_SPEED, "attribute.forgero.mining_speed", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.MINING_LEVEL, "attribute.forgero.mining_level", AttributeDisplayData.Style.BASE_VALUE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR, "attribute.forgero.armor", AttributeDisplayData.Style.ADDITIVE, 0.0f),
			new AttributeDisplayData(DefaultAttributes.ARMOR_TOUGHNESS, "attribute.forgero.armor_toughness", AttributeDisplayData.Style.ADDITIVE, 0.0f)
	);

	private final List<Text> cachedEntries = new ArrayList<>();
	private final SimpleAttributeWriter simpleWriter;
	private final Component component;
	private final AttributeQueryResult attributes;

	public AttributeSectionWriter(Component component, AttributeQueryResult attributeQueryResult) {
		this.component = component;
		this.attributes = attributeQueryResult;
		this.simpleWriter = new SimpleAttributeWriter();
	}

	@Override
	public void append(List<Text> tooltip, TooltipContext context) {
		if (!cachedEntries.isEmpty()) {
			tooltip.add(TooltipTextFormatter.createSectionHeader("tooltip.forgero.attributes"));
			tooltip.addAll(cachedEntries);
			tooltip.add(Text.of(""));
		}
	}

	@Override
	public boolean shouldShow() {
		cachedEntries.clear();

		for (AttributeDisplayData displayData : ATTRIBUTES_TO_DISPLAY) {
			float rawValue = attributes.getValue(displayData.id());

			if (displayData.isDefault(rawValue)) {
				continue;
			}

			// Main attribute line for the final computed value
			cachedEntries.addAll(simpleWriter.createTooltipLines(displayData, rawValue));
		}
		return !cachedEntries.isEmpty();
	}
}
