package com.sigmundgranaas.forgero.tooltip.v2;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.stream.IntStream;

@Getter
@Accessors(fluent = true)
public class WriterHelper {
	private final TooltipConfiguration configuration;

	public WriterHelper(TooltipConfiguration configuration) {
		this.configuration = configuration;
	}

	public MutableText writeBase() {
		MutableText text = Text.literal("");
		IntStream.range(0, configuration.baseIndent()).forEach((integer) -> text.append(Text.literal(" ")));
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
