package com.sigmundgranaas.forgero.minecraft.common.item.tooltip;

import com.sigmundgranaas.forgero.core.soul.Soul;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.minecraft.common.item.tooltip.AttributeWriter.roundFloat;

public class SoulWriter implements Writer {

    private final Soul soul;

    public SoulWriter(Soul soul) {
        this.soul = soul;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        String name = soul.name();
        int level = soul.getLevel();
        float currentXp = soul.getXp();
        int xpTarget = soul.getXpTarget();
        float percentage = (float) currentXp / xpTarget;
        writeSoulInfo(name, level, tooltip);
        writeXpBar(percentage, tooltip);
    }

    private void writeSoulInfo(String name, int level, List<Text> tooltip) {
        Text header = Text.literal(" Soul").formatted(Formatting.GRAY);
        Text info = Text.literal(String.format("  %s, Level %s", name, level));
        tooltip.add(header);
        tooltip.add(info);
    }

    private void writeXpBar(float percentage, List<Text> tooltip) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        String convertBarsToX = IntStream.range(0, 9).mapToObj(i -> percentage > (float) i / 10 ? "X" : "-").reduce("", String::concat);
        String xpBar = String.format("  xp: [%s]", convertBarsToX);
        Text xpText = Text.literal(xpBar).formatted(Formatting.GRAY).append(Text.literal(" " + roundFloat(percentage * 100) + "%"));
        tooltip.add(xpText);
    }
}
