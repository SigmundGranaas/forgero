package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the slots section.
 * <p>
 * Displays available upgrade slots and their status.
 */
public class SlotsSectionWriter extends AbstractSectionWriter {

	public SlotsSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
