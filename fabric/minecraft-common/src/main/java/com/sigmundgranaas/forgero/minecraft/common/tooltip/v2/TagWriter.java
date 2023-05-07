package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;


import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.text.MutableText;

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
