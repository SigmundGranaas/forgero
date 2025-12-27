package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Writes the lore section.
 * <p>
 * Displays flavor text and backstory in italic dark purple.
 */
public class LoreSectionWriter extends AbstractSectionWriter {

	public LoreSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		// Lore uses italic formatting for flavor text
		context.sectionDescriptors().forEach(descriptor -> {
			var argNames = descriptor.getTranslationArgNames();
			if (argNames.isEmpty()) {
				addLine(indented(1)
						.append(Text.translatable(descriptor.template())
								.formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)));
			} else {
				Object[] args = context.valueResolver().resolveArgs(argNames);
				addLine(indented(1)
						.append(Text.translatable(descriptor.template(), args)
								.formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)));
			}
		});
	}
}
