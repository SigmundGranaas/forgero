package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the notes section.
 * <p>
 * Displays usage tips, hints, and additional information about the item.
 */
public class NotesSectionWriter extends AbstractSectionWriter {

	public NotesSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
