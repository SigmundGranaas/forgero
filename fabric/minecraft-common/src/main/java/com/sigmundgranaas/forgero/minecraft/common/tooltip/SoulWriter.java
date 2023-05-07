package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class SoulWriter implements Writer {

	private final Soul soul;

	public SoulWriter(Soul soul) {
		this.soul = soul;
	}

	@Override
	public void write(List<net.minecraft.text.Text> tooltip, TooltipContext context) {
		String name = soul.name();
		int level = soul.getLevel();
		float currentXp = soul.getXp();
		int xpTarget = soul.getXpTarget();
		float percentage = currentXp / xpTarget;
		writeSoulInfo(name, level, tooltip);
		writeXpBar(percentage, tooltip);
	}

	private void writeSoulInfo(String name, int level, List<net.minecraft.text.Text> tooltip) {
		MutableText header = Text.literal(" ").append(Text.translatable("item.forgero.soul").formatted(Formatting.GRAY));
		MutableText info = Text.literal(String.format("  %s, Level %s", name, level));
		tooltip.add(header);
		tooltip.add(info);
	}

	private void writeXpBar(float percentage, List<net.minecraft.text.Text> tooltip) {
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);
		String convertBarsToX = IntStream.range(0, 9).mapToObj(i -> percentage > (float) i / 10 ? "X" : "-").reduce("", String::concat);
		String xpBar = String.format("  xp: [%s]", convertBarsToX);
		MutableText xpText = Text.literal(xpBar).formatted(Formatting.GRAY).append(Text.literal(" " + AttributeWriter.roundFloat(percentage * 100) + "%"));
		tooltip.add(xpText);
	}
}
