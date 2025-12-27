package com.sigmundgranaas.forgero.common.tooltip.api.writer;

import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;

/**
 * Factory for creating section writers.
 * <p>
 * Each section type registers a factory with the section registry.
 * The factory receives context needed to create an appropriate writer
 * for each tooltip rendering.
 *
 * <h2>Registration Example</h2>
 * <pre>{@code
 * TooltipSection section = TooltipSection.of("mymod:custom", 450, true);
 * TooltipApi.registerSection(section, ctx -> new MyCustomWriter(ctx));
 * }</pre>
 *
 * @see com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi#registerSection
 * @see SectionWriterContext
 */
@FunctionalInterface
public interface SectionWriterFactory {

	/**
	 * A factory that always returns an empty writer.
	 */
	SectionWriterFactory EMPTY = ctx -> SectionWriter.EMPTY;

	/**
	 * Creates a writer for the given context.
	 *
	 * @param context Contains all data needed to write the section
	 * @return A new section writer instance
	 */
	SectionWriter create(SectionWriterContext context);
}
