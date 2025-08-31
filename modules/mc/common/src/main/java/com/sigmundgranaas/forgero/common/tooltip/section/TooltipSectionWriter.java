package com.sigmundgranaas.forgero.common.tooltip.section;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

/**
 * A new, clean interface for a modular piece of the Forgero tooltip.
 * Each implementation is responsible for writing a specific "section"
 * like attributes, upgrades, or conditions. This avoids any legacy contracts.
 */
public interface TooltipSectionWriter {
	/**
	 * Appends the section's content to the tooltip list.
	 * This method should only be called if {@link #shouldShow} returns true.
	 *
	 * @param tooltip    The main list of tooltip texts to add to.
	 * @param context    The current tooltip context, for advanced checks.
	 */
	void append(List<Text> tooltip, TooltipContext context);

	/**
	 * Determines if this section has content and should be rendered.
	 * This check is performed before calling {@link #append} to prevent empty sections.
	 *
	 * @return true if the section has content to display, false otherwise.
	 */
	boolean shouldShow();
}
