package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import com.sigmundgranaas.forgero.core.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.CompositeWriter.getRarityFromInt;

public class IngredientSectionWriter extends SectionWriter {
    private static final String sectionTranslationKey = "ingredients";

    private final Constructed container;

    public IngredientSectionWriter(Constructed container) {
        super(TooltipConfiguration.builder().build());
        this.container = container;
    }

    public static Optional<SectionWriter> of(State container) {
        if (container instanceof Constructed constructed) {
            SectionWriter writer = new IngredientSectionWriter(constructed);
            if (writer.shouldWrite()) {
                return Optional.of(writer);
            }
        }
        return Optional.empty();
    }


    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        tooltip.add(createSection(sectionTranslationKey));
        tooltip.addAll(entries());

        super.write(tooltip, context);
    }

    @Override
    public int getSectionOrder() {
        return 0;
    }

    @Override
    public List<Text> entries() {
        List<Text> entries = new ArrayList<>();
        container.parts().forEach(ingredient -> {
            Rarity rarity = getRarityFromInt(AttributeHelper.of(ingredient).rarity());
            MutableText mutableText = indented(entryIndent()).append(Writer.nameToTranslatableText(ingredient)).formatted(rarity.formatting);
            entries.add(mutableText);
        });
        return entries;
    }

    @Override
    public boolean shouldWrite() {
        return container.parts().size() > 0;
    }
}
