package com.sigmundgranaas.forgero.item.tooltip;

import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.state.Composite;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;

import static com.sigmundgranaas.forgero.item.adapter.DescriptionWriter.getRarityFromInt;

public class CompositeWriter implements Writer {
    private final Composite composite;

    public CompositeWriter(Composite composite) {
        this.composite = composite;
    }

    public static void write(Composite composite, List<Text> tooltip, TooltipContext context, int indent) {
        StringBuilder stringIndent = new StringBuilder(" ");
        stringIndent.append(" ".repeat(Math.max(0, indent)));

        if (composite.slots().size() > 0) {
            MutableText slots = Text.literal(stringIndent + "Slots:").formatted(Formatting.GRAY);
            tooltip.add(slots);
            composite.slots().forEach(slot -> {
                MutableText mutableText = Text.literal(String.format("%s%s ", stringIndent + " ", slot.identifier().toLowerCase())).formatted(Formatting.GRAY);
                if (slot.filled()) {
                    Rarity rarity = getRarityFromInt((int) slot.get().get().stream().applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
                    mutableText.append(Text.literal(String.format(": %s", slot.get().get().name())).formatted(rarity.formatting));
                } else {
                    mutableText.append(": -").formatted(Formatting.GRAY);
                }
                tooltip.add(mutableText);
                if (context.isAdvanced() && slot.filled() && slot.get().get() instanceof Composite compositeSlot) {
                    write(compositeSlot, tooltip, context, indent + 2);
                }
            });
        }

        if (composite.ingredients().size() > 0) {
            MutableText ingredients = Text.literal(stringIndent + "Ingredients:").formatted(Formatting.GRAY);
            tooltip.add(ingredients);
            composite.ingredients().forEach(ingredient -> {
                Rarity rarity = getRarityFromInt((int) ingredient.stream().applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
                MutableText mutableText = Text.literal(String.format("%s%s ", stringIndent + " ", ingredient.name().toLowerCase())).formatted(rarity.formatting);
                tooltip.add(mutableText);
                if (context.isAdvanced() && ingredient instanceof Composite compositeSlot) {
                    write(compositeSlot, tooltip, context, indent + 2);
                }
            });
        }
    }

    private void writeCompositeWithIndent(List<Text> tooltip, TooltipContext context, int indent) {
        StringBuilder stringIndent = new StringBuilder(" ");
        stringIndent.append(" ".repeat(Math.max(0, indent)));

        if (composite.slots().size() > 0) {
            MutableText slots = Text.literal(stringIndent + "Slots:").formatted(Formatting.GRAY);
            tooltip.add(slots);
            composite.slots().forEach(slot -> {
                MutableText mutableText = Text.literal(String.format("%s%s ", stringIndent + " ", slot.identifier().toLowerCase())).formatted(Formatting.GRAY);
                if (slot.filled()) {
                    Rarity rarity = getRarityFromInt((int) slot.get().get().stream().applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
                    mutableText.append(Text.literal(String.format(": %s", slot.get().get().name())).formatted(rarity.formatting));
                } else {
                    mutableText.append(": -").formatted(Formatting.GRAY);
                }
                tooltip.add(mutableText);
                if (context.isAdvanced() && slot.filled() && slot.get().get() instanceof Composite compositeSlot) {
                    write(compositeSlot, tooltip, context, indent + 2);
                }
            });
        }

        if (composite.ingredients().size() > 0) {
            MutableText ingredients = Text.literal(stringIndent + "Ingredients:").formatted(Formatting.GRAY);
            tooltip.add(ingredients);
            composite.ingredients().forEach(ingredient -> {
                Rarity rarity = getRarityFromInt((int) ingredient.stream().applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
                MutableText mutableText = Text.literal(String.format("%s%s ", stringIndent + " ", ingredient.name().toLowerCase())).formatted(rarity.formatting);
                tooltip.add(mutableText);
                if (context.isAdvanced() && ingredient instanceof Composite compositeSlot) {
                    write(compositeSlot, tooltip, context, indent + 2);
                }
            });
        }
    }


    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        writeCompositeWithIndent(tooltip, context, 0);
    }
}
