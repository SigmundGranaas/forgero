package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class DefaultWriter implements Writer {
    private final State state;

    public DefaultWriter(State state) {
        this.state = state;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        SlotSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        IngredientSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        ConditionSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        AttributeSectionWriter.of(state).ifPresent(sectionWriter -> sectionWriter.write(tooltip, context));
        UpgradeAttributeSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        FeatureSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        BaseAttributeSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
    }
}
