package com.sigmundgranaas.forgero.minecraft.common.tooltip.writer;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.DefaultWriter;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class PartWriter extends StateWriter {
	public PartWriter(State state) {
		super(state);
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		new DefaultWriter(state).write(tooltip, context);
	}
}
