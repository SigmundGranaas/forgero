package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the subtitle section (headerless).
 * <p>
 * Used for item subtypes or variant names that appear directly below the item name.
 */
public class SubtitleSectionWriter extends AbstractSectionWriter {

	public SubtitleSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
