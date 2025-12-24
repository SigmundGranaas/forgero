package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import com.sigmundgranaas.forgero.common.useinteraction.UseContext;

/**
 * A handler that requires full use lifecycle context including timing information.
 * Use this for handlers that need to know charge time, pull progress, or remaining ticks.
 *
 * <p>Example: Launching a projectile at power based on charge time.</p>
 *
 * @see SimpleUseHandler for handlers needing only basic context
 */
public interface ContextualUseHandler extends UseHandler {

	/**
	 * Applies this handler's effect with full lifecycle context.
	 *
	 * @param context the use context containing world, user, stack, and timing info
	 */
	void apply(UseContext context);
}
