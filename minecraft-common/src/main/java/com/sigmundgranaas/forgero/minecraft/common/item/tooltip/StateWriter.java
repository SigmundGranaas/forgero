package com.sigmundgranaas.forgero.minecraft.common.item.tooltip;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.writer.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.sigmundgranaas.forgero.core.type.Type.*;

public class StateWriter implements Writer {
    protected final State state;

    public StateWriter(State state) {
        this.state = state;
    }

    public static Writer of(State state) {
        if (state.test(AXE)) {
            return new AxeWriter(state);
        } else if (state.test(TOOL)) {
            return new ToolWriter(state);
        } else if (state.test(SWORD_BLADE)) {
            return new SwordBladeWriter(state);
        } else if (state.test(AXE_HEAD)) {
            return new SwordBladeWriter(state);
        } else if (state.test(TOOL_PART_HEAD)) {
            return new AxeHeadWriter(state);
        } else if (state.test(PART)) {
            return new PartWriter((state));
        } else if (state.test(SCHEMATIC)) {
            return new SchematicWriter(state);
        } else if (state.test(GEM) && state instanceof LeveledState leveledState) {
            return new GemWriter(leveledState);
        }
        return new StateWriter(state);
    }


    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if (this.state instanceof Composite composite) {
            new CompositeWriter(composite).write(tooltip, context);
        }
        var active = state.stream().getActiveProperties().toList();
        var passive = state.stream().getPassiveProperties().toList();

        if (active.size() > 0 || passive.size() > 0) {

            MutableText attributes = Text.literal(" ").append(Text.translatable(Writer.toTranslationKey("properties"))).append(": ").formatted(Formatting.GRAY);
            tooltip.add(attributes);

            writePassives(tooltip, context);
            writeActive(tooltip, context);
        }
    }

    private void writePassives(List<Text> tooltip, TooltipContext context) {
        var writer = new PassiveWriter();
        state.stream().getPassiveProperties().forEach(writer::addPassive);
        writer.write(tooltip, context);
    }

    private void writeActive(List<Text> tooltip, TooltipContext context) {
        var writer = new ActiveWriter();
        state.stream().getActiveProperties().forEach(writer::addPassive);
        writer.write(tooltip, context);
    }

}
