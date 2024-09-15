package com.sigmundgranaas.forgero.tooltip.v2;

import com.sigmundgranaas.forgero.tooltip.Writer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.stream.IntStream;

public abstract class BaseWriter implements Writer {
	public static MutableText indented(int level) {
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
