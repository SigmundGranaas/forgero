package com.sigmundgranaas.forgero.common.tooltip.api.writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Writes a section of a Forgero tooltip.
 * <p>
 * Each implementation is responsible for writing a specific section
 * (attributes, upgrades, description, etc.). Sections are written in
 * priority order as determined by their {@link com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection}.
 *
 * <h2>Implementation Guidelines</h2>
 * <ul>
 *   <li>Override {@link #shouldShow()} to check if the section has content</li>
 *   <li>Override {@link #append(List, TooltipContext)} to write the actual content</li>
 *   <li>Use the context provided by {@link SectionWriterFactory} for data access</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class MyCustomSectionWriter implements SectionWriter {
 *     private final SectionWriterContext ctx;
 *
 *     public MyCustomSectionWriter(SectionWriterContext ctx) {
 *         this.ctx = ctx;
 *     }
 *
 *     @Override
 *     public boolean shouldShow() {
 *         return !myData.isEmpty();
 *     }
 *
 *     @Override
 *     public void append(List<Text> tooltip, TooltipContext context) {
 *         tooltip.add(Text.literal("My Section:"));
 *         myData.forEach(item -> tooltip.add(Text.literal("  - " + item)));
 *     }
 * }
 * }</pre>
 *
 * @see SectionWriterFactory
 * @see com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection
 */
public interface SectionWriter {

	/**
	 * A no-op writer that never shows content.
	 */
	SectionWriter EMPTY = new SectionWriter() {
		@Override
		public void append(List<Text> tooltip, TooltipContext context) {
		}

		@Override
		public boolean shouldShow() {
			return false;
		}
	};

	/**
	 * Appends this section's content to the tooltip.
	 * <p>
	 * This method is only called when {@link #shouldShow()} returns true.
	 *
	 * @param tooltip The list to append text lines to
	 * @param context The Minecraft tooltip context
	 */
	void append(List<Text> tooltip, TooltipContext context);

	/**
	 * Determines if this section should be rendered.
	 * <p>
	 * Return false to skip this section entirely (e.g., when there's no content).
	 *
	 * @return true if the section has content to display
	 */
	boolean shouldShow();
}
