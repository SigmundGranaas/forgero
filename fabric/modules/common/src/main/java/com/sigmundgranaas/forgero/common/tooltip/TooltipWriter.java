package com.sigmundgranaas.forgero.common.tooltip;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

@FunctionalInterface
public interface TooltipWriter {
	/**
	 * Appends formatted text lines to the given tooltip list.
	 *
	 * @param tooltip The list of text components that make up the item's tooltip.
	 * @param context The current tooltip context (e.g., NORMAL, ADVANCED).
	 */
	void append(List<Text> tooltip, TooltipContext context);
}
