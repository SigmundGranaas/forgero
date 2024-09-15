package com.sigmundgranaas.forgero.tooltip;

import static com.sigmundgranaas.forgero.core.type.Type.*;

import java.util.List;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.tooltip.v2.DefaultWriter;
import com.sigmundgranaas.forgero.tooltip.v2.ToolWriter;
import com.sigmundgranaas.forgero.tooltip.writer.PartWriter;
import com.sigmundgranaas.forgero.tooltip.writer.SchematicWriter;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class StateWriter implements Writer {
	protected final State state;

	public StateWriter(State state) {
		this.state = state;
	}

	public static Writer of(State state) {
		Writer writer;
		if (state.test(TOOL) || state.test(SWORD)) {
			writer = new ToolWriter(state);
		} else if (state.test(PART)) {
			writer = new PartWriter((state));
		} else if (state.test(SCHEMATIC)) {
			writer = new SchematicWriter(state);
		} else {
			writer = new DefaultWriter(state);
		}
		return CachedWriteHelper.of(state, writer);
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		if (this.state instanceof SoulContainer container) {
			new SoulWriter(container.getSoul()).write(tooltip, context);
		}

		if (this.state instanceof Composite composite) {
			new CompositeWriter(composite).write(tooltip, context);
		}

		if (this.state instanceof Conditional<?> conditional) {
			new ConditionWriter(conditional).write(tooltip, context);
		}

	}
}