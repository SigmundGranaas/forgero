package com.sigmundgranaas.forgero.core.condition.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * An opaque runtime predicate specification.
 *
 * <p>Dynamic conditions describe requirements against live game state (target entity,
 * world, weather, ...). The core compilation layer never evaluates them: it resolves all
 * static conditions during compilation and carries dynamic conditions through as
 * <em>data</em> attached to the compiled result. Evaluation happens exclusively in the
 * game layer, at the call sites that actually hold the relevant game state (event
 * handlers, the effect dispatcher), against the game-side runtime context.
 *
 * <p>This keeps the component tree a pure, compile-time structure: runtime state never
 * enters a component tree. See {@code docs/ADR-002-compiler-in-the-factory.md}.
 *
 * <p>Implementations are typically defined in the Minecraft modules (e.g. entity, block,
 * weather and damage predicates) and registered through the plugin system so they can be
 * parsed from JSON. There they also implement the game-side evaluation interface.
 */
public interface DynamicCondition {
	/**
	 * @return The unique type identifier for this condition (e.g., "minecraft:entity").
	 */
	OpenIdentifier type();
}
