package com.sigmundgranaas.forgerocommon.item.tooltip.writer;

import com.sigmundgranaas.forgerocommon.item.tooltip.AttributeWriter;
import com.sigmundgranaas.forgerocommon.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class AxeWriter extends StateWriter {
    public AxeWriter(State state) {
        super(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        super.write(tooltip, context);
        AttributeWriter.of(AttributeHelper.of(state))
                .addAttribute(AttributeType.ATTACK_DAMAGE)
                .addAttribute(AttributeType.DURABILITY)
                .addAttribute(AttributeType.MINING_SPEED)
                .addAttribute(AttributeType.MINING_LEVEL)
                .addAttribute(AttributeType.RARITY)
                .write(tooltip, context);
    }
}
