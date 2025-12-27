package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the description section.
 * <p>
 * Displays main item descriptions from TooltipDescriptor properties.
 */
public class DescriptionSectionWriter extends AbstractSectionWriter {

	public DescriptionSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
