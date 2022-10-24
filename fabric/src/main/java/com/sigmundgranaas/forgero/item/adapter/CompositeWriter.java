package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.state.Composite;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CompositeWriter {
    public static void write(Composite composite, List<Text> tooltip) {
        if (composite.slots().size() > 0) {
            MutableText slots = Text.literal(" Slots:").formatted(Formatting.GRAY);
            tooltip.add(slots);
            composite.slots().forEach(slot -> {
                MutableText mutableText = Text.literal(String.format("  %s ", slot.identifier().toLowerCase())).formatted(Formatting.GRAY);
                if (slot.filled()) {
                    mutableText.append(Text.literal(String.format(": %s", slot.get().get().name())).formatted(Formatting.GRAY));
                } else {
                    mutableText.append(": empty").formatted(Formatting.GRAY);
                }
                tooltip.add(mutableText);
            });
        }

        if (composite.ingredients().size() > 0) {
            MutableText slots = Text.literal(" Ingredients:").formatted(Formatting.GRAY);
            tooltip.add(slots);
            composite.ingredients().forEach(ingredient -> {
                MutableText mutableText = Text.literal(String.format("  %s ", ingredient.name().toLowerCase())).formatted(Formatting.GRAY);
                tooltip.add(mutableText);
            });
        }
    }
}
