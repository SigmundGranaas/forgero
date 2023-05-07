package com.sigmundgranaas.forgero.minecraft.common.utils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class Text {
	public static net.minecraft.text.MutableText literal(String text) {
		return new LiteralText(text);
	}

	public static net.minecraft.text.MutableText translatable(String text) {
		return new TranslatableText(text);
	}

	public static net.minecraft.text.MutableText empty() {
		return new LiteralText("");
	}


}
