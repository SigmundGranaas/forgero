package com.sigmundgranaas.forgero.minecraft.common.item.tooltip;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public interface Writer {

    static String toTranslationKey(String input) {
        return String.format("item.%s.%s", Forgero.NAMESPACE, input);
    }

    static String toDescriptionKey(State input) {
        return String.format("description.%s.%s", Forgero.NAMESPACE, stateToSeparatedName(input));
    }

    static Text nameToTranslatableText(State state) {
        MutableText text = new LiteralText("");
        for (String element : state.name().split("-")) {
            text.append(new TranslatableText(Writer.toTranslationKey(element)));
            text.append(new TranslatableText("util.forgero.name_separator"));
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

    void write(List<Text> tooltip, TooltipContext context);
}
