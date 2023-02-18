package com.sigmundgranaas.forgero.minecraft.common.tooltip.writer;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.AttributeWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class SwordGuardWriter extends StateWriter {
    public SwordGuardWriter(State state) {
        super(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        super.write(tooltip, context);
        AttributeWriter.of(AttributeHelper.of(state))
                .addAttribute(AttributeType.DURABILITY)
                .addAttribute(AttributeType.RARITY)
                .addAttribute(AttributeType.ATTACK_SPEED)
                .addAttribute(AttributeType.ARMOR)
                .write(tooltip, context);
    }
}
