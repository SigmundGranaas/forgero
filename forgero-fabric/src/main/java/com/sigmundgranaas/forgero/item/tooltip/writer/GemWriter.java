package com.sigmundgranaas.forgero.item.tooltip.writer;

import com.sigmundgranaas.forgero.item.tooltip.AttributeWriter;
import com.sigmundgranaas.forgero.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class GemWriter extends StateWriter {
    public GemWriter(State state) {
        super(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        super.write(tooltip, context);
        AttributeWriter.of(AttributeHelper.of(state))
                .addAttribute(AttributeType.RARITY)
                .addAttribute(AttributeType.DURABILITY)
                .addAttribute(AttributeType.ATTACK_DAMAGE)
                .addAttribute(AttributeType.ATTACK_SPEED)
                .addAttribute(AttributeType.MINING_LEVEL)
                .write(tooltip, context);
    }
}
