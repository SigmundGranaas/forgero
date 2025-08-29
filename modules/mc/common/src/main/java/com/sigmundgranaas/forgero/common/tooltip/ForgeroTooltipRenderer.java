package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * The static entry point for rendering Forgero tooltips.
 * This class orchestrates the conversion from ItemStack to Component,
 * resolves all necessary properties using the standard core AttributeEngine,
 * and then delegates the rendering to a TooltipWriter instance.
 */
public final class ForgeroTooltipRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroTooltipRenderer.class);
	private static ComponentConverter converter;
	private static Resolver resolver;
	private static boolean initialized = false;

	private ForgeroTooltipRenderer() {
	}

	/**
	 * Initializes the renderer with necessary services. Must be called once during mod setup.
	 */
	public static void initialize(ComponentConverter converter, Resolver resolver) {
		if (initialized) {
			LOGGER.warn("ForgeroTooltipRenderer is being initialized more than once.");
			return;
		}
		ForgeroTooltipRenderer.converter = converter;
		ForgeroTooltipRenderer.resolver = resolver;
		ForgeroTooltipRenderer.initialized = true;
	}

	/**
	 * Appends the Forgero-specific tooltip to an item's tooltip list.
	 * This is the primary method to be called from client-side hooks (e.g., mixins).
	 *
	 * @param stack   The ItemStack to generate a tooltip for.
	 * @param tooltip The list of tooltip texts to append to.
	 * @param context The tooltip context provided by Minecraft.
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

		TooltipWriter writer = new ForgeroCompositeTooltipWriter(component, resolver);

		writer.append(tooltip, context);
	}
}
