package com.sigmundgranaas.forgero.item.tooltip;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public interface Writer {

    static String toTranslationKey(String input){
        return String.format("item.%s.%s", Forgero.NAMESPACE, input);
    }

    static Text nameToTranslatableText(State state){
        MutableText text = Text.literal("");
        for (String element: state.name().split("-")) {
            text.append(Text.translatable(Writer.toTranslationKey(element)));
            text.append(Text.literal(" "));
        }
        return text;
    }
    void write(List<Text> tooltip, TooltipContext context);
}
