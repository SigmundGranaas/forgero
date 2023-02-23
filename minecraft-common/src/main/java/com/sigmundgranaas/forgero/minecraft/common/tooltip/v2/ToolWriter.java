package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

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
        List<String> writableAttributes = List.of();
        if (Screen.hasShiftDown()) {
            SlotSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
            IngredientSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
            ConditionSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        }
        AttributeSectionWriter.of(state, TooltipConfiguration.builder().hideZeroValues(false).writableAttributes(writableAttributes).build()).ifPresent(sectionWriter -> sectionWriter.write(tooltip, context));
        FeatureSectionWriter.of(state, TooltipConfiguration.builder().hideSectionTitle(true).build()).ifPresent(writer -> writer.write(tooltip, context));

    }
}
