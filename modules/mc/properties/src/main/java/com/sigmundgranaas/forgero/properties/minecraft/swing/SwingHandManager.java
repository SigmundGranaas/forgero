package com.sigmundgranaas.forgero.properties.minecraft.swing;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
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

	private SwingHandManager() {
		// Static class
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

		List<SwingHandProperty> properties = PropertyDispatcher.active(stack, SwingHandProperty.KEY, DynamicContext.empty());

		for (SwingHandProperty property : properties) {
			for (SwingEffect effect : property.effects()) {
				effect.apply(source, hand);
			}
		}
	}
}
