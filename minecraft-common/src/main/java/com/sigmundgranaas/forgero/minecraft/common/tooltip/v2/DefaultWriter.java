package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
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
        AttributeSectionWriter.of(state).ifPresent(sectionWriter -> sectionWriter.write(tooltip, context));
        BaseAttributeSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
        UpgradeAttributeSectionWriter.of(state).ifPresent(writer -> writer.write(tooltip, context));
    }
}
