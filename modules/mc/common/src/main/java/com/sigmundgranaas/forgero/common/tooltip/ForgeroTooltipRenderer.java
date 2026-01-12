package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi;
import com.sigmundgranaas.forgero.common.tooltip.section.DefaultSections;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * The static entry point for rendering Forgero tooltips.
 * <p>
 * This class orchestrates the conversion from ItemStack to Component,
 * and delegates rendering to the tooltip API.
 *
 * @see TooltipApi
 */
public final class ForgeroTooltipRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroTooltipRenderer.class);
	private static ComponentConverter converter;
	private static boolean initialized = false;

	private ForgeroTooltipRenderer() {
	}

	/**
	 * Initializes the renderer with necessary services.
	 * Must be called once during mod setup.
	 */
	public static void initialize(ComponentConverter converter) {
		if (initialized) {
			LOGGER.warn("ForgeroTooltipRenderer is being initialized more than once. " +
					"This may indicate a lifecycle issue - tooltip renderer should only be initialized once during mod setup.");
			return;
		}
		ForgeroTooltipRenderer.converter = converter;
		ForgeroTooltipRenderer.initialized = true;

		// Register default sections
		DefaultSections.register();
	}

	/**
	 * Appends the Forgero-specific tooltip to an item's tooltip list.
	 *
	 * @param stack   The ItemStack to generate a tooltip for
	 * @param tooltip The list of tooltip texts to append to
	 * @param context The tooltip context provided by Minecraft
	 */
	public static void append(ItemStack stack, List<Text> tooltip, TooltipContext context) {
		if (!initialized || stack.isEmpty()) {
			return;
		}

		Optional<Component> componentOpt = converter.toComponent(stack);
		if (componentOpt.isEmpty()) {
			return;
		}

		Component component = componentOpt.get();

		// Use the new TooltipApi
		TooltipApi.builder(component)
				.appendTo(tooltip, context);
	}

	/**
	 * Checks if the renderer has been initialized.
	 */
	public static boolean isInitialized() {
		return initialized;
	}
}
