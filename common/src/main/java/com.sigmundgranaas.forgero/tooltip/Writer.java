package com.sigmundgranaas.forgero.tooltip;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.Comparator;
import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface Writer {

	static String toTranslationKey(String input) {
		return String.format("item.%s.%s", Forgero.NAMESPACE, input);
	}

	static String toDescriptionKey(State input) {
		return String.format("description.%s.%s", Forgero.NAMESPACE, stateToSeparatedName(input));
	}

	static Text nameToTranslatableText(State state) {
		MutableText text = Text.literal("");
		if (state instanceof Conditional<?> conditional) {
			conditional.namedConditions(conditional.compoundedConditions())
					.stream()
					.max(Comparator.comparingInt(condition -> ComputedAttribute.of(condition, Rarity.KEY).asInt()))
					.map(NamedCondition::name)
					.map(name -> Text.translatable(String.format("condition.forgero.%s", name)))
					.ifPresent(translated -> text.append(translated).append(Text.literal(" ")));
		}
		for (String element : state.name().split("-")) {
			text.append(Text.translatable(Writer.toTranslationKey(element)));
			text.append(Text.translatable("util.forgero.name_separator"));
		}
		return text;
	}

	static Text nameToTranslatableText(String name) {
		MutableText text = Text.literal("");
		for (String element : name.split("-")) {
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

	static Text writeModifierSection(String modifier, String action) {
		return Text.translatable("tooltip.forgero.hold").formatted(Formatting.DARK_GRAY)
				.append(Text.translatable(String.format("tooltip.forgero.%s", modifier)).formatted(Formatting.WHITE))
				.append(Text.translatable(String.format("tooltip.forgero.%s", action)).formatted(Formatting.DARK_GRAY));
	}

	void write(List<Text> tooltip, TooltipContext context);
}