package com.sigmundgranaas.forgero.common.useinteraction;

import net.minecraft.util.ActionResult;

/**
 * Base interface for effects that can be applied during use phases.
 * Each effect represents a single action that can occur during item use.
 *
 * <p>Effects are executed in sequence during each phase (start, tick, release, finish).
 * If any effect returns FAIL, the chain stops and the overall phase fails.</p>
 *
 * <p>This interface lives in modules:mc:common to allow cross-module implementations.
 * Codec registration for effects is handled by the properties module's
 * UseInteractionPropertiesPlugin.</p>
 *
 * <h3>Example implementations:</h3>
 * <ul>
 *   <li>StartUseEffect - Sets current hand to begin use animation</li>
 *   <li>MountProjectileEffect - Mounts arrow to bow state</li>
 *   <li>LaunchProjectileEffect - Fires mounted projectile</li>
 *   <li>ConsumeStackEffect - Consumes items from stack</li>
 *   <li>DamageStackEffect - Applies durability damage</li>
 * </ul>
 *
 * <h3>Implementing a new effect:</h3>
 * <ol>
 *   <li>Create a record implementing UsePhaseEffect</li>
 *   <li>Define a static CODEC field for JSON serialization</li>
 *   <li>Register the effect with UseInteractionPropertiesPlugin.registerEffect()</li>
 * </ol>
 */
public interface UsePhaseEffect {

	/**
	 * Applies this effect within the given use context.
	 *
	 * @param context the use context containing world, user, stack, and timing info
	 * @return the result of applying this effect:
	 *         <ul>
	 *           <li>CONSUME/SUCCESS: Effect applied successfully, continue chain</li>
	 *           <li>PASS: Effect skipped (conditions not met), continue chain</li>
	 *           <li>FAIL: Effect failed, stop chain and fail the phase</li>
	 *         </ul>
	 */
	ActionResult apply(UseContext context);

	/**
	 * @return the type identifier for this effect (e.g., "forgero:start_use")
	 */
	String type();
}
