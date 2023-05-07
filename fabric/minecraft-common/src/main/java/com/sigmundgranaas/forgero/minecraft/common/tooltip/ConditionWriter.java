package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import java.util.List;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class ConditionWriter implements Writer {
	private final Conditional<?> conditional;

	public ConditionWriter(Conditional<?> conditional) {
		this.conditional = conditional;
	}


	@Override
	public void write(List<net.minecraft.text.Text> tooltip, TooltipContext context) {
		var conditions = conditional.namedConditions();
		if (conditions.size() > 0) {
			MutableText header = Text.literal(" ").append(Text.translatable("item.forgero.conditions")).formatted(Formatting.GRAY);
			tooltip.add(header);
			conditions.forEach(condition -> writeCondition(condition, tooltip));
		}

	}

	private void writeCondition(NamedCondition condition, List<net.minecraft.text.Text> tooltip) {
		MutableText conditionText = Text.literal("  ").append(Text.translatable(String.format("item.forgero.%s", condition.name())).formatted(Formatting.WHITE));
		tooltip.add(conditionText);
	}
}
