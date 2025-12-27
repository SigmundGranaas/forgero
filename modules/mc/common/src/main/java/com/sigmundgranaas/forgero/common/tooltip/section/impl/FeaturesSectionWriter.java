package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the features section.
 * <p>
 * Displays special abilities, enchantment-like effects, and other features.
 */
public class FeaturesSectionWriter extends AbstractSectionWriter {

	public FeaturesSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
