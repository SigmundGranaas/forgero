package com.sigmundgranaas.forgero.item.tooltip;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public interface Writer {
    void write(List<Text> tooltip, TooltipContext context);
}
