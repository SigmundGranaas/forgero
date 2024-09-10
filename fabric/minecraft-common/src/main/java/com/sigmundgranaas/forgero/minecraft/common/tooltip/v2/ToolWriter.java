package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.AttributeSectionWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.ConditionSectionWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.FeatureSectionWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.IngredientSectionWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.SlotSectionWriter;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class ToolWriter implements Writer {
	private final State state;

	public ToolWriter(State state) {
		this.state = state;
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		if (!Screen.hasShiftDown()) {
			tooltip.add(Writer.writeModifierSection("shift", "to_show_tool_structure")
			);
		}

		if (Screen.hasShiftDown()) {
			SlotSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
			IngredientSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
			ConditionSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
		}
		AttributeSectionWriter.of(state, TooltipConfiguration.builder().hideZeroValues(false).build())
				.ifPresent(sectionWriter -> sectionWriter.write(tooltip, context));
		FeatureSectionWriter.of(state, TooltipConfiguration.builder().hideSectionTitle(false).build())
				.ifPresent(writer -> writer.write(tooltip, context));
	}

	private List<String> writableAttributes() {
		if (ForgeroConfigurationLoader.configuration.useEntityAttributes) {
			return Collections.emptyList();
		} else {
			return List.of(MiningSpeed.KEY, Durability.KEY, MiningLevel.KEY);
		}
	}
}
