package com.sigmundgranaas.forgero.core.customdata;

/**
 * A supplier of custom data.
 * <p>
 * For customData to be updatable, data container must be supplied when requested.
 * This makes it possible to reload data without restarting the game.
 *
 * @see DataContainer
 */
@FunctionalInterface
public interface DataSupplier {
	DataContainer customData();

	default boolean hasData() {
		return !(customData() instanceof EmptyContainer);
	}
}
