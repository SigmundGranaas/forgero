package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

public class IngredientSectionWriter extends SectionWriter {
    @Override
    public boolean shouldWrite() {
        return false;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {

    }

    @Override
    public int getSectionOrder() {
        return 0;
    }

    @Override
    public List<Text> entries() {
        return null;
    }
}
