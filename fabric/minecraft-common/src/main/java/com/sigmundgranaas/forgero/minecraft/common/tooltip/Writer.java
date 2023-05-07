package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public interface Writer {

	static String toTranslationKey(String input) {
		return String.format("item.%s.%s", Forgero.NAMESPACE, input);
	}

	static String toDescriptionKey(State input) {
		return String.format("description.%s.%s", Forgero.NAMESPACE, stateToSeparatedName(input));
	}

	static net.minecraft.text.Text nameToTranslatableText(State state) {
		MutableText text = Text.literal("");
		for (String element : state.name().split("-")) {
			text.append(Text.translatable(Writer.toTranslationKey(element)));
			text.append(Text.translatable("util.forgero.name_separator"));
		}
		return text;
	}

	private static String stateToSeparatedName(State state) {
		var elements = state.name().split(ELEMENT_SEPARATOR);
		if (elements.length > 1) {
			return elements[0];
		}
		return state.name();
	}

	static net.minecraft.text.Text writeModifierSection(String modifier, String action) {
		return Text.translatable("tooltip.forgero.hold").formatted(Formatting.DARK_GRAY)
				.append(Text.translatable(String.format("tooltip.forgero.%s", modifier)).formatted(Formatting.WHITE))
				.append(Text.translatable(String.format("tooltip.forgero.%s", action)).formatted(Formatting.DARK_GRAY));
	}

	void write(List<net.minecraft.text.Text> tooltip, TooltipContext context);
}
