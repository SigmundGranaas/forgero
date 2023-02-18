package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.stream.IntStream;

public abstract class BaseWriter implements Writer {
    public MutableText indented(int level) {
        MutableText text = Text.literal("");
        IntStream.range(0, level).forEach((integer) -> text.append(Text.literal(" ")));
        return text;
    }


}
