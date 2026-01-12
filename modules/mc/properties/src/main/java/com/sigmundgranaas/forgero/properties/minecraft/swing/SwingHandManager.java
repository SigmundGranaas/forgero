package com.sigmundgranaas.forgero.properties.minecraft.swing;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import com.sigmundgranaas.forgero.effects.entity.SwingEffect;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.List;

/**
 * Manager class for handling hand swing events.
 * Resolves SwingHandProperty from items and executes their effects.
 */
public class SwingHandManager {

	private static ComponentConverter converter;

	private SwingHandManager() {
		// Static class
	}

	/**
	 * Initializes the manager with required services.
	 * Called during Forgero initialization.
	 *
	 * @param services The Forgero services container
	 */
	public static void initialize(ForgeroServices services) {
		converter = services.converter();
	}

	/**
	 * Handles the event when an entity swings their hand with an item.
	 *
	 * @param stack The item stack in the swung hand
	 * @param source The entity that swung their hand
	 * @param hand The hand that was swung
	 */
	public static void handleSwing(ItemStack stack, Entity source, Hand hand) {
		if (stack.isEmpty() || source.getWorld().isClient()) {
			return;
		}

		converter.toComponent(stack).ifPresent(component -> {
			List<SwingHandProperty> properties = getActiveProperties(component);

			for (SwingHandProperty property : properties) {
				for (SwingEffect effect : property.effects()) {
					effect.apply(source, hand);
				}
			}
		});
	}

	private static List<SwingHandProperty> getActiveProperties(Component component) {
		var engine = new SwingHandProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Build context for dynamic condition evaluation
		// You can add more context keys here as needed (entity state, etc.)

		return engine.resolve(component, contextBuilder.build());
	}
}
