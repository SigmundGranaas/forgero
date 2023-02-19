package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.Upgradeable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.CompositeWriter.getRarityFromInt;

public class SlotSectionWriter extends SectionWriter {
    private static final String sectionTranslationKey = "slots";
    private final Upgradeable<?> container;

    public SlotSectionWriter(Upgradeable<?> container) {
        this.container = container;
    }

    public static Optional<SectionWriter> of(Upgradeable<?> container) {
        SectionWriter writer = new SlotSectionWriter(container);
        if (writer.shouldWrite()) {
            return Optional.of(writer);
        }
        return Optional.empty();
    }

    @Override
    public boolean shouldWrite() {
        return container.slots().size() > 0;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        MutableText slots = indented(1).append(Text.translatable(Writer.toTranslationKey("slots")).append(Text.literal(":")).formatted(base()));
        tooltip.add(slots);
        tooltip.addAll(entries());
    }

    @Override
    public int getSectionOrder() {
        return 0;
    }

    @Override
    public List<Text> entries() {
        return container.slots().stream().map(this::writeSlot).flatMap(List::stream).toList();
    }

    private List<Text> writeSlot(Slot slot) {
        MutableText mutableText = indented(2).append(Text.translatable(Writer.toTranslationKey(slot.identifier().toLowerCase())).append(Text.literal(": ")).formatted(base()));
        if (slot.filled()) {
            Rarity rarity = getRarityFromInt(AttributeHelper.of(slot.get().get()).rarity());
            mutableText.append(Writer.nameToTranslatableText(slot.get().get())).formatted(rarity.formatting);
        } else {
            if (slot.identifier().equals(slot.typeName().toLowerCase())) {
                mutableText.append(Text.literal("- ")).formatted(base());
            } else {
                mutableText.append(Text.translatable(Writer.toTranslationKey(slot.typeName().toLowerCase()))).append(Text.literal(" - ")).formatted(base());
            }
            mutableText.append(Text.translatable(String.format("tooltip.forgero.section.%s", slot.category().stream().findFirst().get().toString().toLowerCase())).formatted(base()));
        }
        return List.of(mutableText);
    }
}
