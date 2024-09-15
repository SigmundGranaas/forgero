package com.sigmundgranaas.forgero.tooltip.v2.section;

import com.sigmundgranaas.forgero.tooltip.ConditionalWriter;
import com.sigmundgranaas.forgero.tooltip.v2.BaseWriter;
import com.sigmundgranaas.forgero.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class SectionWriter extends BaseWriter implements ConditionalWriter {

	protected TooltipConfiguration configuration;

	public SectionWriter(TooltipConfiguration configuration) {
		this.configuration = configuration;
	}

	public Text createSection(String sectionName) {
		return translatedSection(sectionName)
				.formatted(sectionFormatting())
				.append(sectionSeparator());

	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		if (configuration.padded()) {
			tooltip.add(Text.empty());
		}
	}

	public int sectionIndent() {
		return configuration.baseIndent();
	}

	public int entryIndent() {
		if (configuration.hideSectionTitle()) {
			return configuration.baseIndent();
		}
		return sectionIndent() + 1;
	}

	public MutableText translatedSection(String sectionName) {
		return Text.translatable(translatableSectionElement(sectionName)).formatted(base());
	}

	public Formatting sectionFormatting() {
		return Formatting.DARK_GRAY;
	}

	private String translatableSectionElement(String element) {
		return String.format("tooltip.forgero.section.%s", element);
	}

	public Text sectionSeparator() {
		return Text.translatable(translatableSectionElement("section_separator"));
	}

	public int getSectionOrder() {
		return configuration.sectionOrder();
	}

	public abstract List<Text> entries();
}