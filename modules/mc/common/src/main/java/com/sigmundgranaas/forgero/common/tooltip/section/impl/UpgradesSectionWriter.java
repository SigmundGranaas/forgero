package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Writes the upgrades section.
 * <p>
 * Displays information about applied upgrades.
 */
public class UpgradesSectionWriter extends AbstractSectionWriter {

	public UpgradesSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		addAllDescriptorLines();
	}
}
