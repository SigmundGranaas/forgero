package com.sigmundgranaas.forgero.tooltip.v2;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public class TagWriter {
	public static MutableText writeTagList(List<String> tags) {
		MutableText text = Text.empty();
		for (int i = 0; i < tags.size(); i++) {
			String tag = tags.get(i);
			MutableText textTag = Text.translatable(String.format("tooltip.forgero.tag.%s", tag.replace(":", ".")));
			if (tags.size() > 1 && i < tags.size() - 1) {
				textTag.append(Text.translatable("tooltip.forgero.list_separator").append(" "));
			}
			text.append(textTag);
		}
		return text;
	}
}
