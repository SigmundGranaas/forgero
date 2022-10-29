package com.sigmundgranaas.forgero.item.tooltip;

import com.sigmundgranaas.forgero.item.tooltip.writer.*;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

import static com.sigmundgranaas.forgero.type.Type.*;

public class StateWriter implements Writer {
    protected final State state;

    public StateWriter(State state) {
        this.state = state;
    }

    public static Writer of(State state) {
        if (state.test(TOOL)) {
            return new ToolWriter(state);
        } else if (state.test(PART)) {
            return new PartWriter((state));
        } else if (state.test(SWORD_BLADE)) {
            return new SwordBladeWriter(state);
        } else if (state.test(TOOL_PART_HEAD)) {
            return new ToolPartHeadWriter(state);
        } else if (state.test(SCHEMATIC)) {
            return new SchematicWriter(state);
        }
        return new StateWriter(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if (this.state instanceof Composite composite) {
            new CompositeWriter(composite).write(tooltip, context);
        }
    }
}
