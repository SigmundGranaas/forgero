package com.sigmundgranaas.forgerocommon.item.tooltip;

import com.sigmundgranaas.forgero.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.property.active.VeinBreaking;
import com.sigmundgranaas.forgerocommon.property.handler.PatternBreaking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ActiveWriter implements Writer {
    private final List<Text> tooltip;
    private final List<ActiveProperty> active;

    public ActiveWriter() {
        this.tooltip = new ArrayList<>();
        this.active = new ArrayList<>();
    }

    private void writeActive(ActiveProperty property) {
        switch (property.getActiveType()) {
            case VEIN_MINING_PATTERN -> veinMining(property, tooltip);
            case BLOCK_BREAKING_PATTERN -> patternMining(property, tooltip);
        }
    }

    private void veinMining(ActiveProperty passive, List<Text> tooltip) {
        if (passive instanceof VeinBreaking veinBreaking) {
            MutableText propertyText = Text.literal("  ")
                    .append(Text.translatable(Writer.toTranslationKey("vein_mining")))
                    .append(Text.literal(": "))
                    .append(Text.translatable("depth "))
                    .append(Text.literal(String.valueOf(veinBreaking.depth())))
                    .formatted(Formatting.WHITE);
            tooltip.add(propertyText);
        }
    }

    private void patternMining(ActiveProperty passive, List<Text> tooltip) {
        if (passive instanceof PatternBreaking pattern) {
            MutableText propertyText = Text.literal("  ")
                    .append(Text.translatable(Writer.toTranslationKey("pattern_mining")))
                    .append(Text.literal(": "))
                    .append(Text.translatable(""))
                    .append(Text.literal(pattern.getPattern()[0].length() + "x" + pattern.getPattern().length))
                    .formatted(Formatting.WHITE);
            tooltip.add(propertyText);
        }
    }


    public ActiveWriter addPassive(ActiveProperty passive) {
        this.active.add(passive);
        return this;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        active.forEach(this::writeActive);
        tooltip.addAll(this.tooltip);
    }
}
