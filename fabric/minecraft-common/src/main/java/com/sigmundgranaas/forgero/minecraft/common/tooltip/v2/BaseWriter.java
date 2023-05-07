package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import java.util.stream.IntStream;

import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public abstract class BaseWriter implements Writer {
	public MutableText indented(int level) {
		MutableText text = Text.literal("");
		IntStream.range(0, level).forEach((integer) -> text.append(Text.literal(" ")));
		return text;
	}

	public Formatting highlighted() {
		return Formatting.WHITE;
	}

	public Formatting neutral() {
		return Formatting.GRAY;
	}

	public Formatting base() {
		return Formatting.DARK_GRAY;
	}

}
