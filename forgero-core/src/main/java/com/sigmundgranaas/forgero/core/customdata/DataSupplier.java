package com.sigmundgranaas.forgero.core.customdata;

import com.sigmundgranaas.forgero.core.property.Target;

/**
 * A supplier of custom data.
 * <p>
 * For customData to be updatable, data container must be supplied when requested.
 * This makes it possible to reload data without restarting the game.
 *
 * @see DataContainer
 */
public interface DataSupplier {
	default DataContainer customData(Target target) {
		return customData();
	}

	default DataContainer customData() {
		return customData(Target.EMPTY);
	}

	default boolean hasData() {
		return !(customData(Target.EMPTY) instanceof EmptyContainer);
	}
}
