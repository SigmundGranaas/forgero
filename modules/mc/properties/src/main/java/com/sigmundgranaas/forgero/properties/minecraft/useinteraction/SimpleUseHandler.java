package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * A handler that can be applied with minimal context: user, stack, and hand.
 * Use this for simple operations that don't need lifecycle timing information.
 *
 * <p>Example: Setting fire duration, applying damage, consuming items.</p>
 *
 * @see ContextualUseHandler for handlers needing full lifecycle context
 */
public interface SimpleUseHandler extends UseHandler {

	/**
	 * Applies this handler's effect.
	 *
	 * @param user  the entity using the item
	 * @param stack the item stack being used
	 * @param hand  the hand holding the item
	 */
	void apply(LivingEntity user, ItemStack stack, Hand hand);
}
