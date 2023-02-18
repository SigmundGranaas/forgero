package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.minecraft.common.tooltip.ConditionalWriter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class SectionWriter extends BaseWriter implements ConditionalWriter {
    public Text createSection(String sectionName) {
        return indented(sectionIndent())
                .append(translatedSection(sectionName))
                .append(sectionSeparator())
                .formatted(sectionFormatting());
    }

    public int sectionIndent() {
        return 1;
    }

    public Text translatedSection(String sectionName) {
        return Text.translatable(translatableSectionElement(sectionName));
    }

    public Formatting sectionFormatting() {
        return Formatting.GRAY;
    }

    private String translatableSectionElement(String element) {
        return String.format("tooltip.forgero.section.%s", element);
    }

    public Text sectionSeparator() {
        return Text.translatable(translatableSectionElement("section_separator"));
    }

    public abstract int getSectionOrder();

    public abstract List<Text> entries();
}
