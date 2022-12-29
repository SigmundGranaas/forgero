package com.sigmundgranaas.forgero.minecraft.common.item.tooltip.writer;

import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.AttributeWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class ToolWriter extends StateWriter {
    public ToolWriter(State state) {
        super(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        super.write(tooltip, context);
        AttributeWriter.of(AttributeHelper.of(state))
                .addAttribute(AttributeType.DURABILITY)
                .addAttribute(AttributeType.MINING_SPEED)
                .addAttribute(AttributeType.MINING_LEVEL)
                .addAttribute(AttributeType.RARITY)
                .write(tooltip, context);
    }
}
