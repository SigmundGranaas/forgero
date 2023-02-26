package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ConditionWriter implements Writer {
    private final Conditional<?> conditional;

    public ConditionWriter(Conditional<?> conditional) {
        this.conditional = conditional;
    }


    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        var conditions = conditional.namedConditions();
        if (conditions.size() > 0) {
            Text header = Text.literal(" ").append(Text.translatable("item.forgero.conditions")).formatted(Formatting.GRAY);
            tooltip.add(header);
            conditions.forEach(condition -> writeCondition(condition, tooltip));
        }

    }

    private void writeCondition(NamedCondition condition, List<Text> tooltip) {
        Text conditionText = Text.literal("  ").append(Text.translatable(String.format("item.forgero.%s", condition.name())).formatted(Formatting.WHITE));
        tooltip.add(conditionText);
    }
}
